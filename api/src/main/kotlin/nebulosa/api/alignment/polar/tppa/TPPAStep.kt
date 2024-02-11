package nebulosa.api.alignment.polar.tppa

import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignmentResult
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.mounts.MountSlewStep
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.fits.Fits
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.plate.solving.PlateSolver

data class TPPAStep(
    private val camera: Camera,
    private val solver: PlateSolver,
    private val request: TPPAStartRequest,
    private val mount: Mount? = null,
    private val longitude: Angle = mount!!.longitude,
    private val latitude: Angle = mount!!.latitude,
    private val cameraRequest: CameraStartCaptureRequest = request.capture,
) : Step {

    private val cameraExposureStep = CameraExposureStep(camera, cameraRequest)
    private val alignment = ThreePointPolarAlignment(solver, longitude, latitude)
    @Volatile private var image: Image? = null
    @Volatile private var mountSlewStep: MountSlewStep? = null
    @Volatile private var stopped = false
    @Volatile private var noSolutionAttempts = 0

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.registerCameraCaptureListener(listener)
    }

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.unregisterCameraCaptureListener(listener)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        cameraExposureStep.beforeJob(jobExecution)
        mount?.tracking(true)
    }

    override fun afterJob(jobExecution: JobExecution) {
        cameraExposureStep.afterJob(jobExecution)

        if (mount != null && request.stopTrackingWhenDone) {
            mount.tracking(false)
        }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (stopped) return StepResult.FINISHED

        LOG.debug { "executing TPPA. camera=$camera, mount=$mount, state=${alignment.state}" }

        if (mount != null) {
            if (alignment.state in 1..2) {
                val step = MountSlewStep(mount, mount.rightAscension + 10.deg, mount.declination)
                mountSlewStep = step
                step.executeSingle(stepExecution)
            }
        }

        if (stopped) return StepResult.FINISHED

        cameraExposureStep.execute(stepExecution)

        if (!stopped) {
            val savedPath = cameraExposureStep.savedPath ?: return StepResult.FINISHED
            image = Fits(savedPath).also(Fits::read).use { image?.load(it, false) ?: Image.open(it, false) }
            val radius = if (mount == null) 0.0 else ThreePointPolarAlignment.DEFAULT_RADIUS
            val result = alignment.align(savedPath, image!!, mount?.rightAscension ?: 0.0, mount?.declination ?: 0.0, radius)

            LOG.info("alignment completed. result=$result")

            if (result is ThreePointPolarAlignmentResult.NeedMoreMeasurement) {
                noSolutionAttempts = 0
                return StepResult.CONTINUABLE
            } else if (result is ThreePointPolarAlignmentResult.NoPlateSolution) {
                noSolutionAttempts++
                return if (noSolutionAttempts < 10) StepResult.CONTINUABLE
                else StepResult.FINISHED
            }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        stopped = true
        mountSlewStep?.stop(mayInterruptIfRunning)
        cameraExposureStep.stop(mayInterruptIfRunning)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TPPAStep>()
    }
}
