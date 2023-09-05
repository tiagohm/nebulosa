package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.batch.core.*
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager

@Component
class CameraCaptureExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val cameraJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
) : JobExecutionListener, StepExecutionListener {

    private val runningJobs = HashMap<String, JobExecution>()

    @Synchronized
    fun execute(camera: Camera, startCapture: CameraStartCaptureRequest): JobExecution {
        val cameraExposureTasklet = CameraExposureTasklet(camera, startCapture)
        val cameraDelayTasklet = CameraDelayTasklet(camera, startCapture.exposureDelay)

        val looping = startCapture.exposureAmount <= 0

        LOG.info("starting capture. request={}", startCapture)

        val cameraCaptureJob = if (looping) {
            JobBuilder("CameraCaptureJob.${camera.name}", jobRepository)
                .start(cameraExposureStep(camera, 1, cameraExposureTasklet))
                .listener(this)
                .build()
        } else {
            val jobBuilder = JobBuilder("CameraCaptureJob.${camera.name}", jobRepository)
                .start(cameraExposureStep(camera, 1, cameraExposureTasklet))

            repeat(startCapture.exposureAmount - 1) {
                val cameraDelayStep = cameraDelayStep(camera, it + 1, cameraDelayTasklet)
                val cameraExposureStep = cameraExposureStep(camera, it + 2, cameraExposureTasklet)

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
            .also { runningJobs[camera.name] = it }
    }

    private fun cameraDelayStep(camera: Camera, stepCount: Int, tasklet: Tasklet) =
        StepBuilder("CameraDelayStep.${camera.name}.$stepCount", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .listener(this)
            .build()

    private fun cameraExposureStep(camera: Camera, stepCount: Int, tasklet: Tasklet) =
        StepBuilder("CameraExposureStep.${camera.name}.$stepCount", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .listener(this)
            .build()

    fun stop(camera: Camera) {
        val job = jobExecutionFor(camera) ?: return
        jobOperator.stop(job.jobId)
    }

    fun isCapturing(camera: Camera): Boolean {
        return jobExecutionFor(camera)?.isRunning ?: false
    }

    fun jobExecutionFor(camera: Camera): JobExecution? {
        return runningJobs[camera.name]
    }

    override fun beforeJob(jobExecution: JobExecution) = Unit

    override fun afterJob(jobExecution: JobExecution) = Unit

    override fun beforeStep(stepExecution: StepExecution) = Unit

    override fun afterStep(stepExecution: StepExecution): ExitStatus = ExitStatus.COMPLETED

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
