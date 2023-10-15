package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraExposureTasklet
import nebulosa.api.cameras.CameraStartCapture
import nebulosa.api.guiding.*
import nebulosa.api.sequencer.SequenceJob
import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.api.sequencer.SequenceJobExecutor
import nebulosa.api.sequencer.SequenceTaskletEvent
import nebulosa.api.sequencer.tasklets.delay.DelayElapsed
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.api.services.MessageService
import nebulosa.common.concurrency.DaemonThreadFactory
import nebulosa.common.concurrency.Incrementer
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.loggerFor
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.ReferenceJobFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.support.SimpleFlow
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
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
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
    private val messageService: MessageService,
    private val executionIncrementer: Incrementer,
) : SequenceJobExecutor<DARVStart>, Consumer<SequenceTaskletEvent> {

    private val runningSequenceJobs = LinkedList<SequenceJob>()

    @Synchronized
    override fun execute(data: DARVStart): SequenceJob {
        val camera = requireNotNull(data.camera)
        val guideOutput = requireNotNull(data.guideOutput)

        if (isRunning(camera, guideOutput)) {
            throw IllegalStateException("DARV Polar Alignment job is already running")
        }

        LOG.info("starting DARV polar alignment. data={}", data)

        val cameraStarCapture = CameraStartCapture(
            camera = camera,
            exposureInMicroseconds = (data.exposureInSeconds + data.initialPauseInSeconds).seconds.inWholeMicroseconds,
        )

        val cameraExposureTasklet = CameraExposureTasklet(cameraStarCapture)

        val cameraExposureStep = StepBuilder("DARVPolarAlignment.Step.CameraExposure.${executionIncrementer.increment()}", jobRepository)
            .tasklet(cameraExposureTasklet, platformTransactionManager)
            .build()

        val cameraExposureFlow = FlowBuilder<SimpleFlow>("DARVPolarAlignment.Flow.CameraExposure.${executionIncrementer.increment()}")
            .start(cameraExposureStep)
            .build()

        val guidePulseDuration = (data.exposureInSeconds / 2.0).seconds
        val initialPulseDelayTasklet = DelayTasklet(data.initialPauseInSeconds.seconds)
        initialPulseDelayTasklet.subscribe(this)

        val direction = if (data.reversed) data.direction.reversed else data.direction

        val forwardGuidePulseTasklet = GuidePulseTasklet(guideOutput, direction, guidePulseDuration)
        forwardGuidePulseTasklet.subscribe(this)

        val backwardGuidePulseTasklet = GuidePulseTasklet(guideOutput, direction.reversed, guidePulseDuration)
        backwardGuidePulseTasklet.subscribe(this)

        val guidePulseFlow = FlowBuilder<SimpleFlow>("DARVPolarAlignment.Flow.GuidePulse.${executionIncrementer.increment()}")
            .start(initialPulseStep(initialPulseDelayTasklet))
            .next(guidePulseStep(forwardGuidePulseTasklet))
            .next(guidePulseStep(backwardGuidePulseTasklet))
            .build()

        val darvJob = JobBuilder("DARVPolarAlignment.Job.${executionIncrementer.increment()}", jobRepository)
            .start(cameraExposureFlow)
            .split(SimpleAsyncTaskExecutor(DaemonThreadFactory))
            .add(guidePulseFlow)
            .end()
            .listener(cameraExposureTasklet)
            .build()

        return jobLauncher
            .run(darvJob, JobParameters())
            .let { DARVSequenceJob(camera, guideOutput, data, darvJob, it) }
            .also(runningSequenceJobs::add)
            .also { jobRegistry.register(ReferenceJobFactory(darvJob)) }
    }

    @Synchronized
    fun stop(camera: Camera, guideOutput: GuideOutput) {
        val jobExecution = jobExecutionFor(camera, guideOutput) ?: return
        jobOperator.stop(jobExecution.jobId)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun jobExecutionFor(camera: Camera, guideOutput: GuideOutput): JobExecution? {
        return sequenceJobFor(camera, guideOutput)?.jobExecution
    }

    private fun initialPulseStep(tasklet: DelayTasklet) =
        StepBuilder("DARVPolarAlignment.Step.InitialPulseDelay.${executionIncrementer.increment()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .build()

    private fun guidePulseStep(tasklet: GuidePulseTasklet) =
        StepBuilder("DARVPolarAlignment.Step.GuidePulse.${executionIncrementer.increment()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .build()

    fun isRunning(camera: Camera, guideOutput: GuideOutput): Boolean {
        return sequenceJobFor(camera, guideOutput)?.jobExecution?.isRunning ?: false
    }

    override fun accept(event: SequenceTaskletEvent) {
        if (event !is SequenceJobEvent) return

        val (camera, guideOutput, data) = sequenceJobWithId(event.jobExecution.jobId) as? DARVSequenceJob ?: return

        val messageEvent = when (event) {
            // Initial pulse event.
            is DelayElapsed -> {
                DARVPolarAlignmentInitialPauseElapsed(camera, guideOutput, event)
            }
            // Forward & backward guide pulse event.
            is GuidePulseEvent -> {
                val tasklet = event.tasklet as GuidePulseTasklet
                val direction = tasklet.direction
                val forward = tasklet.direction == data.direction

                when (event) {
                    is GuidePulseStarted -> {
                        if (forward) DARVPolarAlignmentForwardElapsed(camera, guideOutput, direction, tasklet.duration.inWholeMicroseconds, 0.0)
                        else DARVPolarAlignmentBackwardElapsed(camera, guideOutput, direction, tasklet.duration.inWholeMicroseconds, 0.0)
                    }
                    is GuidePulseElapsed -> {
                        if (forward) DARVPolarAlignmentForwardElapsed(camera, guideOutput, direction, event.remainingTime, event.progress)
                        else DARVPolarAlignmentBackwardElapsed(camera, guideOutput, direction, event.remainingTime, event.progress)
                    }
                    is GuidePulseFinished -> {
                        if (forward) DARVPolarAlignmentForwardElapsed(camera, guideOutput, direction, 0L, 1.0)
                        else DARVPolarAlignmentBackwardElapsed(camera, guideOutput, direction, 0L, 1.0)
                    }
                }
            }
            else -> return
        }

        messageService.sendMessage(messageEvent)
    }

    override fun iterator(): Iterator<SequenceJob> {
        return runningSequenceJobs.iterator()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVPolarAlignmentExecutor>()

        const val DARV_POLAR_ALIGNMENT_ELAPSED = "DARV_POLAR_ALIGNMENT_ELAPSED"
    }
}
