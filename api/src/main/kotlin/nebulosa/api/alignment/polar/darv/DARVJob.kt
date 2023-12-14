package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.GuidePulseListener
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseStep
import nebulosa.api.services.MessageEvent
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
        val job = jobExecution.job as DARVJob
        onNext(DARVStarted(job.camera, job.guideOutput, job.request.initialPause, job.direction))
    }

    override fun afterJob(jobExecution: JobExecution) {
        val job = jobExecution.job as DARVJob
        onNext(DARVFinished(job.camera, job.guideOutput))
    }

    override fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution) {
        val savePath = stepExecution.context[CameraExposureStep.SAVE_PATH] as Path
        onNext(CameraExposureFinished(step.camera, 1.0, savePath))
    }

    override fun onGuidePulseElapsed(step: GuidePulseStep, stepExecution: StepExecution) {
        val job = stepExecution.jobExecution.job as DARVJob
        val direction = step.request.direction
        val remainingTime = stepExecution.context[DelayStep.REMAINING_TIME] as Duration
        val progress = stepExecution.context[DelayStep.PROGRESS] as Double
        val state = if (direction == job.direction) DARVState.FORWARD else DARVState.BACKWARD
        onNext(DARVGuidePulseElapsed(job.camera, job.guideOutput, remainingTime, progress, direction, state))
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val job = stepExecution.jobExecution.job as DARVJob
        val remainingTime = stepExecution.context[DelayStep.REMAINING_TIME] as Duration
        val progress = stepExecution.context[DelayStep.PROGRESS] as Double
        onNext(DARVInitialPauseElapsed(job.camera, job.guideOutput, remainingTime, progress))
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        super.stop(mayInterruptIfRunning)
        close()
    }

    companion object {

        @JvmStatic private val ID = Incrementer()
    }
}
