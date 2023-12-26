package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.*
import nebulosa.api.guiding.GuidePulseListener
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseStep
import nebulosa.api.messages.MessageEvent
import nebulosa.batch.processing.*
import nebulosa.batch.processing.ExecutionContext.Companion.getDouble
import nebulosa.batch.processing.ExecutionContext.Companion.getDuration
import nebulosa.batch.processing.ExecutionContext.Companion.getPath
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.indi.device.camera.FrameType
import java.nio.file.Files
import java.time.Duration

data class DARVJob(
    val request: DARVStartRequest,
) : SimpleJob(), PublishSubscribe<MessageEvent>, CameraCaptureListener, GuidePulseListener, DelayStepListener {

    @JvmField val camera = requireNotNull(request.camera)
    @JvmField val guideOutput = requireNotNull(request.guideOutput)
    @JvmField val direction = if (request.reversed) request.direction.reversed else request.direction

    @JvmField val cameraRequest = (request.capture ?: CameraStartCaptureRequest()).copy(
        camera = camera,
        exposureTime = request.exposureTime + request.initialPause,
        savePath = Files.createTempDirectory("darv"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

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
        val savePath = stepExecution.context.getPath(CameraExposureStep.SAVE_PATH)!!
        onNext(CameraExposureFinished(stepExecution.jobExecution, step.camera, 1, 1, Duration.ZERO, 1.0, Duration.ZERO, savePath))
    }

    override fun onGuidePulseElapsed(step: GuidePulseStep, stepExecution: StepExecution) {
        val direction = step.request.direction
        val remainingTime = stepExecution.context.getDuration(DelayStep.REMAINING_TIME)
        val progress = stepExecution.context.getDouble(DelayStep.PROGRESS)
        val state = if (direction == this.direction) DARVState.FORWARD else DARVState.BACKWARD
        onNext(DARVGuidePulseElapsed(camera, guideOutput, remainingTime, progress, direction, state))
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val remainingTime = stepExecution.context.getDuration(DelayStep.REMAINING_TIME)
        val progress = stepExecution.context.getDouble(DelayStep.PROGRESS)
        onNext(DARVInitialPauseElapsed(camera, guideOutput, remainingTime, progress))
    }
}
