package nebulosa.api.cameras

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.api.services.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.batch.core.Job
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

@Component
class CameraCaptureExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val cameraJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val messageService: MessageService,
) : CameraCaptureEventListener {

    private val runningJobs = ConcurrentHashMap<String, Pair<Job, JobExecution>>()
    private val jobExecutionCounter = AtomicInteger(1)
    private val stepExecutionCounter = AtomicInteger(1)

    @Synchronized
    fun execute(camera: Camera, startCapture: CameraStartCaptureRequest): JobExecution {
        if (isCapturing(camera)) {
            throw IllegalStateException("A job for the camera ${camera.name} is already running")
        }

        LOG.info("starting capture. request={}", startCapture)

        val cameraCaptureJob = if (startCapture.isLoop) {
            val cameraExposureTasklet = CameraLoopExposureTasklet(camera, startCapture, this)

            JobBuilder("CameraCaptureJob.${camera.name}.${jobExecutionCounter.getAndIncrement()}", jobRepository)
                .start(cameraExposureStep(camera, cameraExposureTasklet))
                // .listener(this)
                .listener(cameraExposureTasklet)
                .build()
        } else {
            val cameraExposureTasklet = CameraExposureTasklet(camera, startCapture, this)
            val cameraDelayTasklet = CameraDelayTasklet(camera, startCapture.exposureDelayInSeconds.seconds, cameraExposureTasklet)

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
                // .listener(this)
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
            // .listener(this)
            .build()

    private fun cameraExposureStep(camera: Camera, tasklet: Tasklet) =
        StepBuilder("CameraExposureStep.${camera.name}.${stepExecutionCounter.getAndIncrement()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            // .listener(this)
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

    override fun onCameraCaptureEvent(event: CameraCaptureEvent) {
        when (event) {
            is CameraExposureUpdated -> messageService.sendMessage(CAMERA_EXPOSURE_UPDATED, event)
            is CameraCaptureFinished -> messageService.sendMessage(CAMERA_CAPTURE_FINISHED, event)
            is CameraExposureSaved -> {
                val savedCameraImage = SavedCameraImageEntity.from(event)

                if (event.image == null) {
                    savedCameraImage.id = savedCameraImageRepository.withPath(savedCameraImage.path)?.id ?: savedCameraImage.id
                    savedCameraImageRepository.save(savedCameraImage)
                }

                messageService.sendMessage(CAMERA_IMAGE_SAVED, savedCameraImage)
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()

        const val CAMERA_IMAGE_SAVED = "CAMERA_IMAGE_SAVED"
        const val CAMERA_EXPOSURE_UPDATED = "CAMERA_EXPOSURE_UPDATED"
        const val CAMERA_CAPTURE_FINISHED = "CAMERA_CAPTURE_FINISHED"
    }
}
