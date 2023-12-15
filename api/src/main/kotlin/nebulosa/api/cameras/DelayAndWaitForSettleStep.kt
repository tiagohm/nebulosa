package nebulosa.api.cameras

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.guiding.WaitForSettleListener
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.batch.processing.*
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class DelayAndWaitForSettleStep(
    @JvmField val camera: Camera,
    @JvmField val cameraDelayStep: DelayStep,
    @JvmField val waitForSettleStep: WaitForSettleStep,
) : SimpleSplitStep(cameraDelayStep, waitForSettleStep), PublishSubscribe<CameraCaptureIsSettling>,
    JobExecutionListener, DelayStepListener, WaitForSettleListener {

    @Volatile private var settling = false

    override val subject = PublishSubject.create<CameraCaptureIsSettling>()

    override fun beforeStep(stepExecution: StepExecution) {
        cameraDelayStep.registerDelayStepListener(this)
        waitForSettleStep.registerWaitForSettleListener(this)
    }

    override fun afterStep(stepExecution: StepExecution) {
        cameraDelayStep.unregisterDelayStepListener(this)
        waitForSettleStep.unregisterWaitForSettleListener(this)
    }

    override fun afterJob(jobExecution: JobExecution) {
        close()
    }

    override fun onSettleStarted(step: WaitForSettleStep, stepExecution: StepExecution) {
        settling = true
    }

    override fun onSettleFinished(step: WaitForSettleStep, stepExecution: StepExecution) {
        settling = false
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val canSendEvent = settling && (stepExecution.context[DelayStep.PROGRESS] as Double) < 1.0

        if (canSendEvent) {
            val exposureCount = stepExecution.context[CameraExposureStep.EXPOSURE_COUNT] as Int
            val captureElapsedTime = stepExecution.context[CameraExposureStep.CAPTURE_ELAPSED_TIME] as Duration
            val captureProgress = stepExecution.context[CameraExposureStep.CAPTURE_PROGRESS] as Double
            val captureRemainingTime = stepExecution.context[CameraExposureStep.CAPTURE_REMAINING_TIME] as Duration

            onNext(CameraCaptureIsSettling(camera, 0, exposureCount, captureElapsedTime, captureProgress, captureRemainingTime))
        }
    }
}
