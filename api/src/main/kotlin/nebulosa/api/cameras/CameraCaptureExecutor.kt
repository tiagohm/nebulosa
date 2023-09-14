package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceJob
import nebulosa.api.sequencer.SequenceJobExecutor
import nebulosa.api.services.MessageService
import nebulosa.api.tasklets.delay.DelayTasklet
import nebulosa.common.concurrency.Incrementer
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
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
import java.util.*
import kotlin.time.Duration

@Component
class CameraCaptureExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val asyncJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
    private val messageService: MessageService,
) : SequenceJobExecutor {

    private val runningSequenceJobs = LinkedList<SequenceJob>()
    private val executionIncrementer = Incrementer()

    @Synchronized
    fun execute(
        camera: Camera,
        exposureTime: Duration = Duration.ZERO,
        exposureAmount: Int = 1,
        exposureDelay: Duration = Duration.ZERO,
        x: Int = camera.minX, y: Int = camera.minY,
        width: Int = camera.maxWidth, height: Int = camera.maxHeight,
        frameFormat: String? = null,
        frameType: FrameType = FrameType.LIGHT,
        binX: Int = camera.binX, binY: Int = binX,
        gain: Int = camera.gain, offset: Int = camera.offset,
        autoSave: Boolean = false, savePath: Path? = null,
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
                autoSave, savePath,
            )

            cameraExposureTasklet.subscribe(::onCameraCaptureEvent)

            JobBuilder("CameraCapture.Job.${executionIncrementer.increment()}", jobRepository)
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
                autoSave, savePath,
            )

            cameraExposureTasklet.subscribe(::onCameraCaptureEvent)

            val cameraDelayTasklet = DelayTasklet(exposureDelay)
            cameraDelayTasklet.subscribe(cameraExposureTasklet)

            val jobBuilder = JobBuilder("CameraCapture.Job.${executionIncrementer.increment()}", jobRepository)
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
                .listener(cameraDelayTasklet)
                .build()
        }

        val parameters = JobParametersBuilder()
            .addString("camera", camera.name)
            .addString("exposureTime", "$exposureTime")
            .addString("exposureAmount", "$exposureAmount")
            .addString("exposureDelay", "$exposureDelay")
            .addString("x", "$x")
            .addString("y", "$y")
            .addString("width", "$width")
            .addString("height", "$height")
            .addString("frameFormat", frameFormat ?: "")
            .addString("frameType", "$frameType")
            .addString("binX", "$binX")
            .addString("binY", "$binY")
            .addString("gain", "$gain")
            .addString("offset", "$offset")
            .addString("autoSave", "$autoSave")
            .addString("savePath", "$savePath")
            .toJobParameters()

        return asyncJobLauncher
            .run(cameraCaptureJob, parameters)
            .also { runningSequenceJobs.add(SequenceJob(listOf(camera), cameraCaptureJob, it)) }
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

    private fun onCameraCaptureEvent(event: CameraCaptureEvent) {
        when (event) {
            is CameraCaptureStarted -> messageService.sendMessage(CAMERA_CAPTURE_STARTED, event)
            is CameraCaptureFinished -> messageService.sendMessage(CAMERA_CAPTURE_FINISHED, event)
            is CameraExposureStarted -> messageService.sendMessage(CAMERA_EXPOSURE_STARTED, event)
            is CameraExposureUpdated -> messageService.sendMessage(CAMERA_EXPOSURE_UPDATED, event)
            is CameraExposureDelayElapsed -> messageService.sendMessage(CAMERA_EXPOSURE_DELAY_ELAPSED, event)
            is CameraExposureFinished -> messageService.sendMessage(CAMERA_EXPOSURE_FINISHED, event)
        }
    }

    override fun iterator(): Iterator<SequenceJob> {
        return runningSequenceJobs.iterator()
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
