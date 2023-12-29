package nebulosa.api.cameras

import io.reactivex.rxjava3.core.Observer
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.ExecutionContext.Companion.getBoolean
import nebulosa.batch.processing.ExecutionContext.Companion.getDouble
import nebulosa.batch.processing.ExecutionContext.Companion.getDuration
import nebulosa.batch.processing.ExecutionContext.Companion.getInt
import nebulosa.batch.processing.ExecutionContext.Companion.getPath
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.delay.DelayStep

data class CameraCaptureEventHandler(private val observer: Observer<MessageEvent>) : CameraCaptureListener {

    override fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution) {
        observer.onNext(CameraCaptureStarted(jobExecution, step.camera, step.exposureAmount, step.estimatedCaptureTime, step.exposureTime))
    }

    override fun onExposureStarted(step: CameraExposureStep, stepExecution: StepExecution) {
        sendCameraExposureEvent(step, stepExecution, CameraCaptureState.EXPOSURE_STARTED)
    }

    override fun onExposureElapsed(step: CameraExposureStep, stepExecution: StepExecution) {
        val waiting = stepExecution.context.getBoolean(DelayStep.WAITING)
        val settling = stepExecution.context.getBoolean(WaitForSettleStep.WAITING)
        val state = if (settling) CameraCaptureState.SETTLING
        else if (waiting) CameraCaptureState.WAITING
        else CameraCaptureState.EXPOSURING
        sendCameraExposureEvent(step, stepExecution, state)
    }

    override fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution) {
        sendCameraExposureEvent(step, stepExecution, CameraCaptureState.EXPOSURE_FINISHED)
    }

    override fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution) {
        val captureElapsedTime = jobExecution.context.getDuration(CameraExposureStep.CAPTURE_ELAPSED_TIME)
        observer.onNext(CameraCaptureFinished(jobExecution, step.camera, step.exposureAmount, captureElapsedTime))
    }

    fun sendCameraExposureEvent(step: CameraExposureStep, stepExecution: StepExecution, state: CameraCaptureState) {
        val exposureCount = stepExecution.context.getInt(CameraExposureStep.EXPOSURE_COUNT)
        val captureElapsedTime = stepExecution.context.getDuration(CameraExposureStep.CAPTURE_ELAPSED_TIME)
        val captureProgress = stepExecution.context.getDouble(CameraExposureStep.CAPTURE_PROGRESS)
        val captureRemainingTime = stepExecution.context.getDuration(CameraExposureStep.CAPTURE_REMAINING_TIME)

        val event = when (state) {
            CameraCaptureState.WAITING,
            CameraCaptureState.SETTLING -> {
                val waitProgress = stepExecution.context.getDouble(DelayStep.PROGRESS)
                val waitRemainingTime = stepExecution.context.getDuration(DelayStep.REMAINING_TIME)

                CameraCaptureIsWaiting(
                    stepExecution.jobExecution, step.camera,
                    step.exposureAmount, exposureCount, captureElapsedTime, captureProgress, captureRemainingTime,
                    waitProgress, waitRemainingTime, state
                )
            }
            CameraCaptureState.EXPOSURING -> {
                val exposureProgress = stepExecution.context.getDouble(CameraExposureStep.EXPOSURE_PROGRESS)
                val exposureRemainingTime = stepExecution.context.getDuration(CameraExposureStep.EXPOSURE_REMAINING_TIME)

                CameraExposureElapsed(
                    stepExecution.jobExecution, step.camera,
                    step.exposureAmount, exposureCount, captureElapsedTime, captureProgress, captureRemainingTime,
                    exposureProgress, exposureRemainingTime
                )
            }
            CameraCaptureState.EXPOSURE_STARTED -> {
                val exposureRemainingTime = stepExecution.context.getDuration(CameraExposureStep.EXPOSURE_REMAINING_TIME)

                CameraExposureStarted(
                    stepExecution.jobExecution, step.camera,
                    step.exposureAmount, exposureCount, captureElapsedTime,
                    captureProgress, captureRemainingTime, exposureRemainingTime
                )
            }
            CameraCaptureState.EXPOSURE_FINISHED -> {
                val savePath = stepExecution.context.getPath(CameraExposureStep.SAVE_PATH)!!

                CameraExposureFinished(
                    stepExecution.jobExecution, step.camera,
                    step.exposureAmount, exposureCount,
                    captureElapsedTime, captureProgress, captureRemainingTime,
                    savePath
                )
            }
            else -> return
        }

        observer.onNext(event)
    }
}
