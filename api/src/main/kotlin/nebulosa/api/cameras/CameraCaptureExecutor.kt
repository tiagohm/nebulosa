package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.batch.core.*
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class CameraCaptureExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val cameraJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
) : JobExecutionListener, StepExecutionListener {

    private val runningJobs = ConcurrentHashMap<String, Pair<Job, JobExecution>>()
    private val jobExecutionCounter = AtomicInteger(1)
    private val stepExecutionCounter = AtomicInteger(1)

    @Synchronized
    fun execute(camera: Camera, startCapture: CameraStartCaptureRequest): JobExecution {
        if (isCapturing(camera)) {
            throw IllegalStateException("the camera ${camera.name} capture job already is running")
        }

        val cameraExposureTasklet = CameraExposureTasklet(camera, startCapture)
        val cameraDelayTasklet = CameraDelayTasklet(camera, startCapture.exposureDelay)

        val isLooping = startCapture.exposureAmount <= 0

        LOG.info("starting capture. request={}", startCapture)

        val cameraCaptureJob = if (isLooping) {
            JobBuilder("CameraCaptureJob.${camera.name}.${jobExecutionCounter.getAndIncrement()}", jobRepository)
                .start(cameraExposureStep(camera, cameraExposureTasklet))
                .listener(this)
                .build()
        } else {
            val jobBuilder = JobBuilder("CameraCaptureJob.${camera.name}.${jobExecutionCounter.getAndIncrement()}", jobRepository)
                .start(cameraExposureStep(camera, cameraExposureTasklet))

            repeat(startCapture.exposureAmount - 1) {
                val cameraDelayStep = cameraDelayStep(camera, cameraDelayTasklet)
                val cameraExposureStep = cameraExposureStep(camera, cameraExposureTasklet)

                jobBuilder
                    .next(cameraDelayStep)
                    .next(cameraExposureStep)
            }

            jobBuilder
                .listener(this)
                .listener(cameraExposureTasklet)
                .build()
        }

        return cameraJobLauncher
            .run(cameraCaptureJob, JobParameters())
            .also { runningJobs[camera.name] = cameraCaptureJob to it }
            .also { jobRegistry.register(ReferenceJobFactory(cameraCaptureJob)) }
    }

    private fun cameraDelayStep(camera: Camera, tasklet: Tasklet) =
        StepBuilder("CameraDelayStep.${camera.name}.${stepExecutionCounter.getAndIncrement()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .listener(this)
            .build()

    private fun cameraExposureStep(camera: Camera, tasklet: Tasklet) =
        StepBuilder("CameraExposureStep.${camera.name}.${stepExecutionCounter.getAndIncrement()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .listener(this)
            .build()

    fun stop(camera: Camera) {
        val jobExecution = jobExecutionFor(camera) ?: return
        jobOperator.stop(jobExecution.jobId)
    }

    fun isCapturing(camera: Camera): Boolean {
        return jobExecutionFor(camera)?.isRunning ?: false
    }

    private fun jobExecutionFor(camera: Camera): JobExecution? {
        return runningJobs[camera.name]?.second
    }

    override fun beforeJob(jobExecution: JobExecution) = Unit

    override fun afterJob(jobExecution: JobExecution) = Unit

    override fun beforeStep(stepExecution: StepExecution) = Unit

    override fun afterStep(stepExecution: StepExecution): ExitStatus = ExitStatus.COMPLETED

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
