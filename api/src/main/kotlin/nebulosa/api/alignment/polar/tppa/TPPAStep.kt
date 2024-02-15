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
import nebulosa.common.concurrency.latch.Pauseable
import nebulosa.common.time.Stopwatch
import nebulosa.fits.fits
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.plate.solving.PlateSolver

data class TPPAStep(
    @JvmField val camera: Camera,
    private val solver: PlateSolver,
    private val request: TPPAStartRequest,
    @JvmField val mount: Mount? = null,
    private val longitude: Angle = mount!!.longitude,
    private val latitude: Angle = mount!!.latitude,
    private val cameraRequest: CameraStartCaptureRequest = request.capture,
) : Step, Pauseable {

    private val cameraExposureStep = CameraExposureStep(camera, cameraRequest)
    private val alignment = ThreePointPolarAlignment(solver, longitude, latitude)
    private val listeners = LinkedHashSet<TPPAListener>()
    private val stopwatch = Stopwatch()
    private val stepDistances = DoubleArray(2) { if (request.eastDirection) request.stepDistance else -request.stepDistance }

    @Volatile private var image: Image? = null
    @Volatile private var mountSlewStep: MountSlewStep? = null
    @Volatile private var noSolutionAttempts = 0
    @Volatile private var stepExecution: StepExecution? = null

    val stepCount
        get() = alignment.state

    val elapsedTime
        get() = stopwatch.elapsed

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.registerCameraCaptureListener(listener)
    }

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.unregisterCameraCaptureListener(listener)
    }

    fun registerTPPAListener(listener: TPPAListener): Boolean {
        return listeners.add(listener)
    }

    fun unregisterTPPAListener(listener: TPPAListener): Boolean {
        return listeners.remove(listener)
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

        stopwatch.stop()

        listeners.forEach { it.polarAlignmentFinished(this, jobExecution.cancellationToken.isCancelled) }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        val cancellationToken = stepExecution.jobExecution.cancellationToken

        if (cancellationToken.isCancelled) return StepResult.FINISHED

        LOG.debug { "executing TPPA. camera=$camera, mount=$mount, state=${alignment.state}" }

        this.stepExecution = stepExecution

        if (cancellationToken.isPaused) {
            listeners.forEach { it.polarAlignmentPaused(this) }
            cancellationToken.waitIfPaused()
        }

        if (cancellationToken.isCancelled) return StepResult.FINISHED

        stopwatch.start()

        // Mount slew step.
        if (mount != null) {
            if (alignment.state in 1..2 && stepDistances[alignment.state - 1] != 0.0) {
                val step = MountSlewStep(mount, mount.rightAscension + stepDistances[alignment.state - 1].deg, mount.declination)
                mountSlewStep = step
                listeners.forEach { it.slewStarted(this, step.rightAscension, step.declination) }
                step.executeSingle(stepExecution)
                stepDistances[alignment.state - 1] = 0.0
            }
        }

        if (cancellationToken.isCancelled) return StepResult.FINISHED

        listeners.forEach { it.solverStarted(this) }

        // Camera capture step.
        cameraExposureStep.execute(stepExecution)

        if (!cancellationToken.isCancelled) {
            val savedPath = cameraExposureStep.savedPath ?: return StepResult.FINISHED
            image = savedPath.fits().let { image?.load(it, false) ?: Image.open(it, false) }

            val radius = if (mount == null) 0.0 else ThreePointPolarAlignment.DEFAULT_RADIUS

            // Polar alignment step.
            val result = alignment.align(
                savedPath, image!!, mount?.rightAscension ?: 0.0, mount?.declination ?: 0.0, radius,
                request.compensateRefraction, cancellationToken
            )

            LOG.info("alignment completed. result=$result, cancelled={}", cancellationToken.isCancelled)

            if (cancellationToken.isCancelled) return StepResult.FINISHED

            when (result) {
                is ThreePointPolarAlignmentResult.NeedMoreMeasurement -> {
                    noSolutionAttempts = 0
                    listeners.forEach { it.solverFinished(this, result.rightAscension, result.declination) }
                    return StepResult.CONTINUABLE
                }
                is ThreePointPolarAlignmentResult.NoPlateSolution -> {
                    noSolutionAttempts++

                    return if (noSolutionAttempts < 10) {
                        listeners.forEach { it.solverFailed(this) }
                        StepResult.CONTINUABLE
                    } else {
                        StepResult.FINISHED
                    }
                }
                is ThreePointPolarAlignmentResult.Measured -> {
                    noSolutionAttempts = 0

                    listeners.forEach {
                        it.solverFinished(this, result.rightAscension, result.declination)
                        it.polarAlignmentComputed(this, result.azimuth, result.altitude)
                    }

                    return StepResult.CONTINUABLE
                }
            }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        mountSlewStep?.stop(mayInterruptIfRunning)
        cameraExposureStep.stop(mayInterruptIfRunning)
    }

    override val isPaused
        get() = stepExecution?.jobExecution?.cancellationToken?.isPaused ?: false

    override fun pause() {
        stopwatch.stop()
    }

    override fun unpause() {
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TPPAStep>()
    }
}
