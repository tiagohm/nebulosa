package nebulosa.api.alignment.polar.darv

import nebulosa.api.alignment.polar.darv.DARVPolarAlignmentState.BACKWARD
import nebulosa.api.alignment.polar.darv.DARVPolarAlignmentState.FORWARD
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraCaptureListener
import nebulosa.api.cameras.CameraExposureStep
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.GuidePulseListener
import nebulosa.api.sequencer.*
import nebulosa.api.services.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobExecutionListener
import nebulosa.batch.processing.JobLauncher
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.delay.DelayListener
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.*

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
class DARVPolarAlignmentExecutor(
    private val jobLauncher: JobLauncher,
    private val messageService: MessageService,
    private val capturesPath: Path,
) : JobExecutionListener, CameraCaptureListener, GuidePulseListener, DelayListener {

    private val jobExecutions = HashMap<Pair<Camera, GuideOutput>, JobExecution>(1)

    @Synchronized
    fun execute(request: DARVStart) {
        val camera = requireNotNull(request.camera)
        val guideOutput = requireNotNull(request.guideOutput)

        if (isRunning(camera, guideOutput)) {
            throw IllegalStateException("DARV Polar Alignment job is already running")
        }

        LOG.info("starting DARV polar alignment. data={}", request)

        val cameraRequest = CameraStartCaptureRequest(
            camera = camera,
            exposureTime = request.exposureTime + request.initialPause,
            savePath = Path.of("$capturesPath", "${camera.name}-DARV.fits")
        )

        val darvJob = DARVPolarAlignmentJob(request, cameraRequest)
        val jobExecution = jobLauncher.launch(darvJob)
        jobExecutions[camera to guideOutput] = jobExecution
    }

    @Synchronized
    fun stop(camera: Camera, guideOutput: GuideOutput) {
        val jobExecution = jobExecutions[camera to guideOutput] ?: return
        jobExecution.stop()
    }

    override fun accept(event: SequenceTaskletEvent) {
        if (event !is SequenceJobEvent) {
            LOG.warn("unaccepted sequence task event: {}", event)
            return
        }

        val (camera, guideOutput, data) = sequenceJobWithId(event.jobExecution.jobId) ?: return

        val messageEvent = when (event) {
            // Initial pulse event.
            is DelayElapsed -> DARVPolarAlignmentInitialPauseElapsed(camera, guideOutput, event)
            // Forward & backward guide pulse event.
            is GuidePulseEvent -> {
                val direction = event.tasklet.request.direction
                val duration = event.tasklet.request.duration
                val state = if ((direction == data.direction) != data.reversed) FORWARD else BACKWARD
                DARVPolarAlignmentGuidePulseElapsed(camera, guideOutput, state, direction, duration, event.progress, event.jobExecution)
            }
            is CameraCaptureEvent -> event
            else -> return
        }

        messageService.sendMessage(messageEvent)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        val (camera, guideOutput) = sequenceJobWithId(jobExecution.jobId) ?: return
        messageService.sendMessage(DARVPolarAlignmentStarted(camera, guideOutput, jobExecution))
    }

    override fun afterJob(jobExecution: JobExecution) {
        val (camera, guideOutput) = sequenceJobWithId(jobExecution.jobId) ?: return
        messageService.sendMessage(DARVPolarAlignmentFinished(camera, guideOutput, jobExecution))
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
        TODO("Not yet implemented")
    }

    override fun onDelayElapsed(stepExecution: StepExecution) {
        TODO("Not yet implemented")
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVPolarAlignmentExecutor>()
    }
}
