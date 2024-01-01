package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayStep

data class CameraLoopExposureStep(
    override val request: CameraStartCaptureRequest,
) : CameraStartCaptureStep {

    private val cameraExposureStep = CameraExposureStep(request)
    private val delayStep = DelayStep(request.exposureDelay)

    override val savedPath
        get() = cameraExposureStep.savedPath

    init {
        delayStep.registerDelayStepListener(cameraExposureStep)
    }

    override fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.registerCameraCaptureListener(listener)
    }

    override fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return cameraExposureStep.unregisterCameraCaptureListener(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        cameraExposureStep.execute(stepExecution)
        delayStep.execute(stepExecution)
        return StepResult.CONTINUABLE
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        cameraExposureStep.stop(mayInterruptIfRunning)
        delayStep.stop(mayInterruptIfRunning)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        cameraExposureStep.beforeJob(jobExecution)
        delayStep.beforeJob(jobExecution)
    }

    override fun afterJob(jobExecution: JobExecution) {
        cameraExposureStep.afterJob(jobExecution)
        delayStep.afterJob(jobExecution)
    }
}
