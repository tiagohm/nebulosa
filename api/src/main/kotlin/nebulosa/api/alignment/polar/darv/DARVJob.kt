package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.GuidePulseListener
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseStep
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.*
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.common.concurrency.Incrementer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

data class DARVJob(
    val request: DARVStartRequest,
) : SimpleJob(), PublishSubscribe<MessageEvent>, CameraCaptureListener, GuidePulseListener, DelayStepListener {

    @JvmField val camera = requireNotNull(request.camera)
    @JvmField val guideOutput = requireNotNull(request.guideOutput)
    @JvmField val direction = if (request.reversed) request.direction.reversed else request.direction

    @JvmField val cameraRequest = CameraStartCaptureRequest(
        camera = camera,
        exposureTime = request.exposureTime + request.initialPause,
        savePath = Files.createTempDirectory("darv"),
    )

    override val id = "DARV.Job.${ID.increment()}"

    override val subject = PublishSubject.create<MessageEvent>()

    init {
        val cameraExposureStep = CameraExposureStep(cameraRequest)
        cameraExposureStep.registerCameraCaptureListener(this)

        val initialPauseDelayStep = DelayStep(request.initialPause)
        initialPauseDelayStep.registerDelayStepListener(this)

        val guidePulseDuration = request.exposureTime.dividedBy(2L)
        val forwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction, guidePulseDuration)
        val forwardGuidePulseStep = GuidePulseStep(forwardGuidePulseRequest)
        forwardGuidePulseStep.registerGuidePulseListener(this)

        val backwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction.reversed, guidePulseDuration)
        val backwardGuidePulseStep = GuidePulseStep(backwardGuidePulseRequest)
        backwardGuidePulseStep.registerGuidePulseListener(this)

        val guideFlow = SimpleFlowStep(initialPauseDelayStep, forwardGuidePulseStep, backwardGuidePulseStep)
        add(SimpleSplitStep(cameraExposureStep, guideFlow))
    }

    override fun beforeJob(jobExecution: JobExecution) {
        onNext(DARVStarted(camera, guideOutput, request.initialPause, direction))
    }

    override fun afterJob(jobExecution: JobExecution) {
        onNext(DARVFinished(camera, guideOutput))
    }

    override fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution) {
        val savePath = stepExecution.context[CameraExposureStep.SAVE_PATH] as Path
        onNext(CameraExposureFinished(step.camera, 1, 1, Duration.ZERO, 1.0, Duration.ZERO, savePath))
    }

    override fun onGuidePulseElapsed(step: GuidePulseStep, stepExecution: StepExecution) {
        val direction = step.request.direction
        val remainingTime = stepExecution.context[DelayStep.REMAINING_TIME] as Duration
        val progress = stepExecution.context[DelayStep.PROGRESS] as Double
        val state = if (direction == this.direction) DARVState.FORWARD else DARVState.BACKWARD
        onNext(DARVGuidePulseElapsed(camera, guideOutput, remainingTime, progress, direction, state))
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val remainingTime = stepExecution.context[DelayStep.REMAINING_TIME] as Duration
        val progress = stepExecution.context[DelayStep.PROGRESS] as Double
        onNext(DARVInitialPauseElapsed(camera, guideOutput, remainingTime, progress))
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        super.stop(mayInterruptIfRunning)
        close()
    }

    companion object {

        @JvmStatic private val ID = Incrementer()
    }
}
