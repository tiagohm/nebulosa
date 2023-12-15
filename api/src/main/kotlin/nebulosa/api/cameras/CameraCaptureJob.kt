package nebulosa.api.cameras

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.guiding.DitherAfterExposureStep
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.PublishSubscribe
import nebulosa.batch.processing.SimpleJob
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.common.concurrency.Incrementer
import nebulosa.guiding.Guider
import java.nio.file.Path
import java.time.Duration

data class CameraCaptureJob(
    private val request: CameraStartCaptureRequest,
    private val guider: Guider,
) : SimpleJob(), PublishSubscribe<MessageEvent>, CameraCaptureListener {

    @JvmField val camera = requireNotNull(request.camera)

    private val cameraExposureStep = if (request.isLoop) CameraLoopExposureStep(request)
    else CameraExposureStep(request)

    override val id = "CameraCapture.Job.${ID.increment()}"

    override val subject = PublishSubject.create<MessageEvent>()

    init {
        if (cameraExposureStep is CameraExposureStep) {
            val waitForSettleStep = WaitForSettleStep(guider)
            val ditherStep = DitherAfterExposureStep(request.dither, guider)
            val cameraDelayStep = DelayStep(request.exposureDelay)
            val delayAndWaitForSettleStep = DelayAndWaitForSettleStep(camera, cameraDelayStep, waitForSettleStep)

            cameraDelayStep.registerDelayStepListener(cameraExposureStep)
            delayAndWaitForSettleStep.subscribe(this)

            add(waitForSettleStep)
            add(cameraExposureStep)

            repeat(request.exposureAmount - 1) {
                add(delayAndWaitForSettleStep)
                add(cameraExposureStep)
                add(ditherStep)
            }
        } else {
            add(cameraExposureStep)
        }

        cameraExposureStep.registerCameraCaptureListener(this)
    }

    override fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution) {
        onNext(CameraCaptureStarted(step.camera, step.exposureAmount, step.estimatedCaptureTime, step.exposureTime))
    }

    override fun onExposureStarted(step: CameraExposureStep, stepExecution: StepExecution) {
        sendCameraExposureEvent(step, stepExecution, CameraCaptureState.EXPOSURE_STARTED)
    }

    override fun onExposureElapsed(step: CameraExposureStep, stepExecution: StepExecution) {
        val waiting = stepExecution.context[CameraExposureStep.CAPTURE_WAITING] as Boolean
        val state = if (waiting) CameraCaptureState.WAITING else CameraCaptureState.EXPOSURING
        sendCameraExposureEvent(step, stepExecution, state)
    }

    override fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution) {
        sendCameraExposureEvent(step, stepExecution, CameraCaptureState.EXPOSURE_FINISHED)
    }

    override fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution) {
        val captureElapsedTime = jobExecution.context[CameraExposureStep.CAPTURE_ELAPSED_TIME] as Duration
        onNext(CameraCaptureFinished(step.camera, step.exposureAmount, captureElapsedTime))
    }

    fun sendCameraExposureEvent(step: CameraExposureStep, stepExecution: StepExecution, state: CameraCaptureState) {
        val exposureCount = stepExecution.context[CameraExposureStep.EXPOSURE_COUNT] as Int
        val captureElapsedTime = stepExecution.context[CameraExposureStep.CAPTURE_ELAPSED_TIME] as Duration
        val captureProgress = stepExecution.context[CameraExposureStep.CAPTURE_PROGRESS] as Double
        val captureRemainingTime = stepExecution.context[CameraExposureStep.CAPTURE_REMAINING_TIME] as Duration

        val event = when (state) {
            CameraCaptureState.WAITING -> {
                val waitProgress = stepExecution.context[DelayStep.PROGRESS] as Double
                val waitRemainingTime = stepExecution.context[DelayStep.REMAINING_TIME] as Duration

                CameraCaptureIsWaiting(
                    step.camera,
                    step.exposureAmount, exposureCount, captureElapsedTime, captureProgress, captureRemainingTime,
                    waitProgress, waitRemainingTime
                )
            }
            CameraCaptureState.SETTLING -> {
                CameraCaptureIsSettling(step.camera, step.exposureAmount, exposureCount, captureElapsedTime, captureProgress, captureRemainingTime)
            }
            CameraCaptureState.EXPOSURING -> {
                val exposureProgress = stepExecution.context[CameraExposureStep.EXPOSURE_PROGRESS] as Double
                val exposureRemainingTime = stepExecution.context[CameraExposureStep.EXPOSURE_REMAINING_TIME] as Duration

                CameraExposureElapsed(
                    step.camera,
                    step.exposureAmount, exposureCount, captureElapsedTime, captureProgress, captureRemainingTime,
                    exposureProgress, exposureRemainingTime
                )
            }
            CameraCaptureState.EXPOSURE_STARTED -> {
                val exposureRemainingTime = stepExecution.context[CameraExposureStep.EXPOSURE_REMAINING_TIME] as Duration

                CameraExposureStarted(
                    step.camera,
                    step.exposureAmount, exposureCount, captureElapsedTime,
                    captureProgress, captureRemainingTime, exposureRemainingTime
                )
            }
            CameraCaptureState.EXPOSURE_FINISHED -> {
                val savePath = stepExecution.context[CameraExposureStep.SAVE_PATH] as Path

                CameraExposureFinished(
                    step.camera,
                    step.exposureAmount, exposureCount,
                    captureElapsedTime, captureProgress, captureRemainingTime,
                    savePath
                )
            }
            else -> return
        }

        onNext(event)
    }

    companion object {

        @JvmStatic private val ID = Incrementer()
    }
}
