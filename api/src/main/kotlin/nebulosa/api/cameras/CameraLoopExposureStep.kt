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

    init {
        delayStep.registerListener(cameraExposureStep)
    }

    override fun registerListener(listener: CameraCaptureListener) {
        cameraExposureStep.registerListener(listener)
    }

    override fun unregisterListener(listener: CameraCaptureListener) {
        cameraExposureStep.unregisterListener(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        cameraExposureStep.execute(stepExecution)
        delayStep.execute(stepExecution)
        return StepResult.CONTINUABLE
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        cameraExposureStep.stop()
        delayStep.stop()
    }

    override fun beforeJob(jobExecution: JobExecution) {
        cameraExposureStep.beforeJob(jobExecution)
    }

    override fun afterJob(jobExecution: JobExecution) {
        cameraExposureStep.afterJob(jobExecution)
    }
}
