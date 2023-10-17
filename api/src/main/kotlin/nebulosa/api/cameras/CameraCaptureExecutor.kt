package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.sequencer.SequenceJobExecutor
import nebulosa.api.sequencer.SequenceJobFactory
import nebulosa.api.services.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.ReferenceJobFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.stereotype.Component
import java.util.*

@Component
class CameraCaptureExecutor(
    private val jobOperator: JobOperator,
    private val asyncJobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
    private val messageService: MessageService,
    private val sequenceJobFactory: SequenceJobFactory,
) : SequenceJobExecutor<CameraStartCaptureRequest, CameraSequenceJob>, Consumer<CameraCaptureEvent> {

    private val runningSequenceJobs = LinkedList<CameraSequenceJob>()

    @Synchronized
    override fun execute(request: CameraStartCaptureRequest): CameraSequenceJob {
        val camera = requireNotNull(request.camera)

        check(!isCapturing(camera)) { "job is already running for camera: [${camera.name}]" }
        check(camera.connected) { "camera is not connected" }

        LOG.info("starting camera capture. data={}", request)

        val cameraCaptureJob = if (request.isLoop) {
            sequenceJobFactory.cameraLoopCapture(request, this)
        } else {
            sequenceJobFactory.cameraCapture(request, this)
        }

        return asyncJobLauncher
            .run(cameraCaptureJob, JobParameters())
            .let { CameraSequenceJob(camera, request, cameraCaptureJob, it) }
            .also(runningSequenceJobs::add)
            .also { jobRegistry.register(ReferenceJobFactory(cameraCaptureJob)) }
    }

    fun stop(camera: Camera) {
        val jobExecution = jobExecutionFor(camera) ?: return
        jobOperator.stop(jobExecution.id)
    }

    fun isCapturing(camera: Camera): Boolean {
        return sequenceJobFor(camera)?.jobExecution?.isRunning ?: false
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun jobExecutionFor(camera: Camera): JobExecution? {
        return sequenceJobFor(camera)?.jobExecution
    }

    override fun accept(event: CameraCaptureEvent) {
        messageService.sendMessage(event)
    }

    override fun iterator(): Iterator<CameraSequenceJob> {
        return runningSequenceJobs.iterator()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
