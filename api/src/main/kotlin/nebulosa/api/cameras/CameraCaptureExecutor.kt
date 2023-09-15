package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceJob
import nebulosa.api.sequencer.SequenceJobExecutor
import nebulosa.api.services.MessageService
import nebulosa.api.tasklets.delay.DelayTasklet
import nebulosa.common.concurrency.Incrementer
import nebulosa.indi.device.camera.Camera
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
) : SequenceJobExecutor<CameraCaptureRequest> {

    private val runningSequenceJobs = LinkedList<SequenceJob>()
    private val executionIncrementer = Incrementer()

    @Synchronized
    override fun execute(data: CameraCaptureRequest): SequenceJob {
        val camera = requireNotNull(data.camera)

        if (isCapturing(camera)) {
            throw IllegalStateException("A Camera Exposure job is already running. camera=${camera.name}")
        }

        val cameraCaptureJob = if (data.isLoop) {
            val cameraExposureTasklet = CameraLoopExposureTasklet(data)
            cameraExposureTasklet.subscribe(::onCameraCaptureEvent)

            JobBuilder("CameraCapture.Job.${executionIncrementer.increment()}", jobRepository)
                .start(cameraExposureStep(cameraExposureTasklet))
                .listener(cameraExposureTasklet)
                .build()
        } else {
            val cameraExposureTasklet = CameraExposureTasklet(data)

            cameraExposureTasklet.subscribe(::onCameraCaptureEvent)

            val cameraDelayTasklet = DelayTasklet(data.exposureDelayInSeconds.seconds)
            cameraDelayTasklet.subscribe(cameraExposureTasklet)

            val jobBuilder = JobBuilder("CameraCapture.Job.${executionIncrementer.increment()}", jobRepository)
                .start(cameraExposureStep(cameraExposureTasklet))

            repeat(data.exposureAmount - 1) {
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
            .addString("exposureTime", "${data.exposureInMicroseconds}")
            .addString("exposureAmount", "${data.exposureAmount}")
            .addString("exposureDelay", "${data.exposureDelayInSeconds}")
            .addString("x", "${data.x}")
            .addString("y", "${data.y}")
            .addString("width", "${data.width}")
            .addString("height", "${data.height}")
            .addString("frameFormat", data.frameFormat ?: "")
            .addString("frameType", "${data.frameType}")
            .addString("binX", "${data.binX}")
            .addString("binY", "${data.binY}")
            .addString("gain", "${data.gain}")
            .addString("offset", "${data.offset}")
            .addString("autoSave", "${data.autoSave}")
            .addString("savePath", "${data.savePath}")
            .toJobParameters()

        return asyncJobLauncher
            .run(cameraCaptureJob, parameters)
            .let { SequenceJob(listOf(camera), cameraCaptureJob, it) }
            .also { runningSequenceJobs.add(it) }
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
