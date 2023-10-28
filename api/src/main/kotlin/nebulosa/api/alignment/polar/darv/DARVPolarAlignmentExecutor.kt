package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.guiding.*
import nebulosa.api.sequencer.*
import nebulosa.api.sequencer.tasklets.delay.DelayElapsed
import nebulosa.api.services.MessageService
import nebulosa.common.concurrency.Incrementer
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.loggerFor
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.ReferenceJobFactory
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
class DARVPolarAlignmentExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
    private val messageService: MessageService,
    private val jobIncrementer: Incrementer,
    private val capturesPath: Path,
    private val sequenceFlowFactory: SequenceFlowFactory,
    private val sequenceTaskletFactory: SequenceTaskletFactory,
    private val simpleAsyncTaskExecutor: SimpleAsyncTaskExecutor,
) : SequenceJobExecutor<DARVStart, DARVSequenceJob>, Consumer<SequenceTaskletEvent>, JobExecutionListener {

    private val runningSequenceJobs = LinkedList<DARVSequenceJob>()

    @Synchronized
    override fun execute(request: DARVStart): DARVSequenceJob {
        val camera = requireNotNull(request.camera)
        val guideOutput = requireNotNull(request.guideOutput)

        if (isRunning(camera, guideOutput)) {
            throw IllegalStateException("DARV Polar Alignment job is already running")
        }

        LOG.info("starting DARV polar alignment. data={}", request)

        val cameraRequest = CameraStartCaptureRequest(
            camera = camera,
            exposureInMicroseconds = (request.exposureInSeconds + request.initialPauseInSeconds).seconds.inWholeMicroseconds,
            savePath = Path.of("$capturesPath", "${camera.name}-DARV.fits")
        )

        val cameraExposureTasklet = sequenceTaskletFactory.cameraExposure(cameraRequest)
        cameraExposureTasklet.subscribe(this)
        val cameraExposureFlow = sequenceFlowFactory.cameraExposure(cameraExposureTasklet)

        val guidePulseDuration = (request.exposureInSeconds / 2.0).seconds.inWholeMilliseconds
        val initialPauseDelayTasklet = sequenceTaskletFactory.delay(request.initialPauseInSeconds.seconds)
        initialPauseDelayTasklet.subscribe(this)

        val direction = if (request.reversed) request.direction.reversed else request.direction

        val forwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction, guidePulseDuration)
        val forwardGuidePulseTasklet = sequenceTaskletFactory.guidePulse(forwardGuidePulseRequest)
        forwardGuidePulseTasklet.subscribe(this)

        val backwardGuidePulseRequest = GuidePulseRequest(guideOutput, direction.reversed, guidePulseDuration)
        val backwardGuidePulseTasklet = sequenceTaskletFactory.guidePulse(backwardGuidePulseRequest)
        backwardGuidePulseTasklet.subscribe(this)

        val guidePulseFlow = sequenceFlowFactory.guidePulse(initialPauseDelayTasklet, forwardGuidePulseTasklet, backwardGuidePulseTasklet)

        val darvJob = JobBuilder("DARVPolarAlignment.Job.${jobIncrementer.increment()}", jobRepository)
            .start(cameraExposureFlow)
            .split(simpleAsyncTaskExecutor)
            .add(guidePulseFlow)
            .end()
            .listener(this)
            .listener(cameraExposureTasklet)
            .build()

        return jobLauncher
            .run(darvJob, JobParameters())
            .let { DARVSequenceJob(camera, guideOutput, request, darvJob, it) }
            .also(runningSequenceJobs::add)
            .also { jobRegistry.register(ReferenceJobFactory(darvJob)) }
    }

    @Synchronized
    fun stop(camera: Camera, guideOutput: GuideOutput) {
        val jobExecution = jobExecutionFor(camera, guideOutput) ?: return
        jobOperator.stop(jobExecution.id)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun jobExecutionFor(camera: Camera, guideOutput: GuideOutput): JobExecution? {
        return sequenceJobFor(camera, guideOutput)?.jobExecution
    }

    fun isRunning(camera: Camera, guideOutput: GuideOutput): Boolean {
        return sequenceJobFor(camera, guideOutput)?.jobExecution?.isRunning ?: false
    }

    override fun accept(event: SequenceTaskletEvent) {
        if (event !is SequenceJobEvent) {
            LOG.warn("unaccepted sequence task event: {}", event)
            return
        }

        val (camera, guideOutput, data) = sequenceJobWithId(event.jobExecution.jobId) ?: return

        val messageEvent = when (event) {
            // Initial pulse event.
            is DelayElapsed -> {
                DARVPolarAlignmentInitialPauseElapsed(camera, guideOutput, event)
            }
            // Forward & backward guide pulse event.
            is GuidePulseEvent -> {
                val direction = event.tasklet.request.direction
                val duration = event.tasklet.request.durationInMilliseconds
                val state = if ((direction == data.direction) != data.reversed) DARVPolarAlignmentState.FORWARD
                else DARVPolarAlignmentState.BACKWARD

                when (event) {
                    is GuidePulseStarted -> {
                        DARVPolarAlignmentGuidePulseElapsed(camera, guideOutput, state, direction, duration, 0.0)
                    }
                    is GuidePulseElapsed -> {
                        DARVPolarAlignmentGuidePulseElapsed(camera, guideOutput, state, direction, event.remainingTime, event.progress)
                    }
                    is GuidePulseFinished -> {
                        DARVPolarAlignmentGuidePulseElapsed(camera, guideOutput, state, direction, 0L, 1.0)
                    }
                }
            }
            is CameraCaptureEvent -> event
            else -> return
        }

        messageService.sendMessage(messageEvent)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        val (camera, guideOutput) = sequenceJobWithId(jobExecution.jobId) ?: return
        messageService.sendMessage(DARVPolarAlignmentStarted(camera, guideOutput))
    }

    override fun afterJob(jobExecution: JobExecution) {
        val (camera, guideOutput) = sequenceJobWithId(jobExecution.jobId) ?: return
        messageService.sendMessage(DARVPolarAlignmentFinished(camera, guideOutput))
    }

    override fun iterator(): Iterator<DARVSequenceJob> {
        return runningSequenceJobs.iterator()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVPolarAlignmentExecutor>()
    }
}
