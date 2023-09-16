package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.sequencer.SequenceJob
import nebulosa.api.sequencer.SequenceJobExecutor
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.api.services.MessageService
import nebulosa.common.concurrency.Incrementer
import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.ReferenceJobFactory
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Component
class CameraCaptureExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val asyncJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
    private val messageService: MessageService,
    private val executionIncrementer: Incrementer,
) : SequenceJobExecutor<CameraCaptureRequest>, Consumer<CameraCaptureEvent> {

    private val runningSequenceJobs = LinkedList<SequenceJob>()

    @Synchronized
    override fun execute(data: CameraCaptureRequest): SequenceJob {
        val camera = requireNotNull(data.camera)

        if (isCapturing(camera)) {
            throw IllegalStateException("A Camera Exposure job is already running. camera=${camera.name}")
        }

        LOG.info("starting camera capture. data={}", data)

        val cameraCaptureJob = if (data.isLoop) {
            val cameraExposureTasklet = CameraLoopExposureTasklet(data)
            cameraExposureTasklet.subscribe(this)

            JobBuilder("CameraCapture.Job.${executionIncrementer.increment()}", jobRepository)
                .start(cameraExposureStep(cameraExposureTasklet))
                .listener(cameraExposureTasklet)
                .build()
        } else {
            val cameraExposureTasklet = CameraExposureTasklet(data)
            cameraExposureTasklet.subscribe(this)

            val jobBuilder = JobBuilder("CameraCapture.Job.${executionIncrementer.increment()}", jobRepository)
                .start(cameraExposureStep(cameraExposureTasklet))

            val hasDelay = data.exposureDelayInSeconds in 1L..60L
            val cameraDelayTasklet = DelayTasklet(data.exposureDelayInSeconds.seconds)
            cameraDelayTasklet.subscribe(cameraExposureTasklet)

            repeat(data.exposureAmount - 1) {
                if (hasDelay) {
                    val cameraDelayStep = cameraDelayStep(cameraDelayTasklet)
                    jobBuilder.next(cameraDelayStep)
                }

                val cameraExposureStep = cameraExposureStep(cameraExposureTasklet)
                jobBuilder.next(cameraExposureStep)
            }

            jobBuilder
                .listener(cameraExposureTasklet)
                .listener(cameraDelayTasklet)
                .build()
        }

        return asyncJobLauncher
            .run(cameraCaptureJob, JobParameters())
            .let { SequenceJob(listOf(camera), cameraCaptureJob, it) }
            .also(runningSequenceJobs::add)
            .also { jobRegistry.register(ReferenceJobFactory(cameraCaptureJob)) }
    }

    private fun cameraDelayStep(tasklet: Tasklet) =
        StepBuilder("CameraCapture.Step.Delay.${executionIncrementer.increment()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .build()

    private fun cameraExposureStep(tasklet: Tasklet) =
        StepBuilder("CameraCapture.Step.Exposure.${executionIncrementer.increment()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .build()

    fun stop(camera: Camera) {
        val jobExecution = jobExecutionFor(camera) ?: return
        jobOperator.stop(jobExecution.jobId)
    }

    fun isCapturing(camera: Camera): Boolean {
        return sequenceTaskFor(camera)?.jobExecution?.isRunning ?: false
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun jobExecutionFor(camera: Camera): JobExecution? {
        return sequenceTaskFor(camera)?.jobExecution
    }

    override fun accept(event: CameraCaptureEvent) {
        messageService.sendMessage(event)
    }

    override fun iterator(): Iterator<SequenceJob> {
        return runningSequenceJobs.iterator()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
