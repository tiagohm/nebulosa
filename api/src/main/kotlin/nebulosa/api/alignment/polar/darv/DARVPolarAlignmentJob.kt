package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.GuidePulseListener
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseStep
import nebulosa.api.sequencer.ObservableJob
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.SimpleFlowStep
import nebulosa.batch.processing.SimpleSplitStep
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.delay.DelayListener
import nebulosa.batch.processing.delay.DelayStep

data class DARVPolarAlignmentJob(
    val request: DARVStart,
    val cameraRequest: CameraStartCaptureRequest,
) : ObservableJob<DARVPolarAlignmentEvent>(), CameraCaptureListener, GuidePulseListener, DelayListener {

    @JvmField val camera = requireNotNull(request.camera)
    @JvmField val guideOutput = requireNotNull(request.guideOutput)

    override val id = "DARVPolarAlignment.Job.${System.currentTimeMillis()}"

    init {
        val cameraExposureStep = CameraExposureStep(cameraRequest)
        cameraExposureStep.registerListener(this)

        val guidePulseDuration = request.exposureTime.dividedBy(2L)
        val initialPauseDelayStep = DelayStep(request.initialPause)
        initialPauseDelayStep.registerListener(this)

        val direction = if (request.reversed) request.direction.reversed else request.direction

        val forwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction, guidePulseDuration)
        val forwardGuidePulseStep = GuidePulseStep(forwardGuidePulseRequest)
        forwardGuidePulseStep.registerListener(this)

        val backwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction.reversed, guidePulseDuration)
        val backwardGuidePulseStep = GuidePulseStep(backwardGuidePulseRequest)
        backwardGuidePulseStep.registerListener(this)

        val guideFlow = SimpleFlowStep(initialPauseDelayStep, forwardGuidePulseStep, backwardGuidePulseStep)
        add(SimpleSplitStep(cameraExposureStep, guideFlow))
    }

    override fun beforeJob(jobExecution: JobExecution) {
        onNext(DARVPolarAlignmentStarted(jobExecution))
    }

    override fun afterJob(jobExecution: JobExecution) {
        onNext(DARVPolarAlignmentFinished(jobExecution))
    }

    override fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution) {
        TODO("Not yet implemented")
    }

    override fun onExposureStarted(stepExecution: StepExecution) {
        TODO("Not yet implemented")
    }

    override fun onExposureElapsed(stepExecution: StepExecution) {
        TODO("Not yet implemented")
    }

    override fun onExposureFinished(stepExecution: StepExecution) {
        TODO("Not yet implemented")
    }

    override fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution) {
        TODO("Not yet implemented")
    }

    override fun onGuidePulseElapsed(stepExecution: StepExecution) {
        // val direction = event.tasklet.request.direction
        // val duration = event.tasklet.request.duration
        // val state = if ((direction == data.direction) != data.reversed) FORWARD else BACKWARD
        onNext(DARVPolarAlignmentGuidePulseElapsed(stepExecution.jobExecution, DARVPolarAlignmentState.FORWARD))
    }

    override fun onDelayElapsed(stepExecution: StepExecution) {
        onNext(DARVPolarAlignmentInitialPauseElapsed(stepExecution.jobExecution))
    }
}
