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
import nebulosa.math.deg
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.StarDetector

data class TPPAStep(
    private val camera: Camera,
    private val solver: PlateSolver,
    private val starDetector: StarDetector<Image>,
    private val request: TPPAStartRequest,
    private val mount: Mount? = null,
    private val cameraRequest: CameraStartCaptureRequest = request.capture,
) : Step {

    private val cameraExposureStep = CameraExposureStep(camera, cameraRequest)
    private val alignment = ThreePointPolarAlignment(solver, starDetector)
    @Volatile private var image: Image? = null

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.registerCameraCaptureListener(listener)
    }

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.unregisterCameraCaptureListener(listener)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        mount?.tracking(true)
    }

    override fun afterJob(jobExecution: JobExecution) {
        if (mount != null && request.stopTrackingWhenDone) {
            mount.tracking(false)
        }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (mount != null) {
            if (alignment.state == ThreePointPolarAlignment.State.SECOND_MEASURE ||
                alignment.state == ThreePointPolarAlignment.State.THIRD_MEASURE
            ) {
                val slewStep = MountSlewStep(mount, mount.rightAscension + 10.deg, mount.declination)
                slewStep.execute(stepExecution)
            }
        }

        cameraExposureStep.execute(stepExecution)

        val savedPath = cameraExposureStep.savedPath ?: return StepResult.FINISHED
        image = Fits(savedPath).also(Fits::read).use { image?.load(it, false) ?: Image.open(it, false) }
        val radius = if (mount == null) 0.0 else ThreePointPolarAlignment.DEFAULT_RADIUS
        val result = alignment.align(savedPath, image!!, mount?.rightAscension ?: 0.0, mount?.declination ?: 0.0, radius)

        if (result === ThreePointPolarAlignmentResult.NeedMoreMeasure) {
            return StepResult.CONTINUABLE
        } else if (result === ThreePointPolarAlignmentResult.NoPlateSolution) {
            return StepResult.FINISHED
        }

        return StepResult.FINISHED
    }
}
