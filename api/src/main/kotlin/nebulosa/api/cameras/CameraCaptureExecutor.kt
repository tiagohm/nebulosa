package nebulosa.api.cameras

import nebulosa.api.services.MessageService
import nebulosa.api.tasklets.delay.DelayTasklet
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
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
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

@Component
class CameraCaptureExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val cameraJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
    private val messageService: MessageService,
) : CameraCaptureListener {

    private val runningJobs = ConcurrentHashMap<String, Pair<Job, JobExecution>>()
    private val executionCounter = AtomicInteger(1)

    @Synchronized
    fun execute(
        camera: Camera,
        exposureTime: Duration = Duration.ZERO,
        exposureAmount: Int = 1, // 0 = looping
        exposureDelay: Duration = Duration.ZERO,
        x: Int = camera.minX, y: Int = camera.minY,
        width: Int = camera.maxWidth, height: Int = camera.maxHeight,
        frameFormat: String? = null,
        frameType: FrameType = FrameType.LIGHT,
        binX: Int = camera.binX, binY: Int = binX,
        gain: Int = camera.gain, offset: Int = camera.offset,
        autoSave: Boolean = false,
        savePath: Path? = null,
    ): JobExecution {
        if (isCapturing(camera)) {
            throw IllegalStateException("A Camera Exposure job is already running. camera=${camera.name}")
        }

        val cameraCaptureJob = if (exposureAmount <= 0) {
            val cameraExposureTasklet = CameraLoopExposureTasklet(
                camera,
                exposureTime, exposureDelay,
                x, y, width, height,
                frameFormat, frameType,
                binX, binY, gain, offset,
                autoSave, savePath, this,
            )

            JobBuilder("CameraCapture.Job.${executionCounter.getAndIncrement()}", jobRepository)
                .start(cameraExposureStep(cameraExposureTasklet))
                .listener(cameraExposureTasklet)
                .build()
        } else {
            val cameraExposureTasklet = CameraExposureTasklet(
                camera,
                exposureTime, exposureAmount, exposureDelay,
                x, y, width, height,
                frameFormat, frameType,
                binX, binY, gain, offset,
                autoSave, savePath, this,
            )

            val cameraDelayTasklet = DelayTasklet(exposureDelay, cameraExposureTasklet)

            val jobBuilder = JobBuilder("CameraCapture.Job.${executionCounter.getAndIncrement()}", jobRepository)
                .start(cameraExposureStep(cameraExposureTasklet))

            repeat(exposureAmount - 1) {
                val cameraDelayStep = cameraDelayStep(cameraDelayTasklet)
                val cameraExposureStep = cameraExposureStep(cameraExposureTasklet)

                jobBuilder
                    .next(cameraDelayStep)
                    .next(cameraExposureStep)
            }

            jobBuilder
                .listener(cameraExposureTasklet)
                .build()
        }

        return cameraJobLauncher
            .run(cameraCaptureJob, JobParameters())
            .also { runningJobs[camera.name] = cameraCaptureJob to it }
            .also { jobRegistry.register(ReferenceJobFactory(cameraCaptureJob)) }
    }

    private fun cameraDelayStep(tasklet: Tasklet) =
        StepBuilder("CameraCapture.Step.Delay.${executionCounter.getAndIncrement()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
            .build()

    private fun cameraExposureStep(tasklet: Tasklet) =
        StepBuilder("CameraCapture.Step.Exposure.${executionCounter.getAndIncrement()}", jobRepository)
            .tasklet(tasklet, platformTransactionManager)
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

    override fun onCameraCaptureStarted(camera: Camera) {
        messageService.sendMessage(CAMERA_CAPTURE_STARTED, camera)
    }

    override fun onCameraCaptureFinished(camera: Camera) {
        messageService.sendMessage(CAMERA_CAPTURE_FINISHED, camera)
    }

    override fun onCameraExposureStarted(camera: Camera, exposureCount: Int) {
        messageService.sendMessage(CAMERA_EXPOSURE_STARTED, CameraExposureStartEvent(camera, exposureCount))
    }

    override fun onCameraExposureFinished(camera: Camera, image: Image?, path: Path?) {
        messageService.sendMessage(CAMERA_EXPOSURE_FINISHED, CameraExposureFinishEvent(camera, path))
    }

    override fun onCameraExposureUpdated(
        camera: Camera,
        exposureAmount: Int, exposureCount: Int, exposureTime: Duration, exposureRemainingTime: Duration, exposureProgress: Double,
        captureTime: Duration, captureRemainingTime: Duration, captureProgress: Double,
        looping: Boolean, elapsedTime: Duration,
    ) {
        val event = CameraExposureUpdateEvent(
            camera, exposureAmount, exposureCount, exposureTime.inWholeMicroseconds,
            exposureRemainingTime.inWholeMicroseconds, exposureProgress, captureTime.inWholeMicroseconds, captureRemainingTime.inWholeMicroseconds,
            captureProgress, looping, elapsedTime.inWholeMicroseconds,
        )

        messageService.sendMessage(CAMERA_EXPOSURE_UPDATED, event)
    }

    override fun onCameraExposureDelayElapsed(camera: Camera, waitProgress: Double, waitRemainingTime: Duration, waitTime: Duration) {
        val event = CameraExposureDelayEvent(camera, waitProgress, waitRemainingTime.inWholeMicroseconds, waitTime.inWholeMicroseconds)
        messageService.sendMessage(CAMERA_EXPOSURE_DELAY_ELAPSED, event)
    }

    companion object {

        const val CAMERA_CAPTURE_STARTED = "CAMERA_CAPTURE_STARTED"
        const val CAMERA_CAPTURE_FINISHED = "CAMERA_CAPTURE_FINISHED"
        const val CAMERA_EXPOSURE_STARTED = "CAMERA_EXPOSURE_STARTED"
        const val CAMERA_EXPOSURE_UPDATED = "CAMERA_EXPOSURE_UPDATED"
        const val CAMERA_EXPOSURE_DELAY_ELAPSED = "CAMERA_EXPOSURE_DELAY_ELAPSED"
        const val CAMERA_EXPOSURE_FINISHED = "CAMERA_EXPOSURE_FINISHED"
    }
}
