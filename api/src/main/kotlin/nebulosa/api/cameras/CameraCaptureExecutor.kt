package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.log.debug
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component
import java.util.*

@Component
class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val jobLauncher: JobLauncher,
) : Consumer<MessageEvent> {

    private val jobExecutions = LinkedList<JobExecution>()

    @Synchronized
    fun execute(request: CameraStartCaptureRequest) {
        val camera = requireNotNull(request.camera)

        check(camera.connected) { "camera is not connected" }
        check(!isCapturing(camera)) { "job is already running for camera: [${camera.name}]" }

        LOG.debug { "starting camera capture. request=$request" }

        val cameraCaptureJob = CameraCaptureJob(request, guider)
        cameraCaptureJob.subscribe(this)
        jobExecutions.add(jobLauncher.launch(cameraCaptureJob))
    }

    fun findJobExecution(camera: Camera): JobExecution? {
        for (i in jobExecutions.indices.reversed()) {
            val jobExecution = jobExecutions[i]
            val job = jobExecution.job as CameraCaptureJob

            if (!jobExecution.isDone && job.camera === camera) {
                return jobExecution
            }
        }

        return null
    }

    @Synchronized
    fun stop(camera: Camera) {
        val jobExecution = findJobExecution(camera) ?: return
        jobLauncher.stop(jobExecution)
    }

    fun isCapturing(camera: Camera): Boolean {
        return findJobExecution(camera) != null
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
