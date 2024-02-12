package nebulosa.api.cameras

import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecutor
import nebulosa.batch.processing.JobLauncher
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.log.info
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component

@Component
class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    override val jobLauncher: JobLauncher,
) : JobExecutor() {

    fun execute(camera: Camera, request: CameraStartCaptureRequest): String {
        check(camera.connected) { "camera is not connected" }
        check(findJobExecutionWithAny(camera) == null) { "Camera Capture job is already running" }

        LOG.info { "starting camera capture. camera=$camera, request=$request" }

        val cameraCaptureJob = CameraCaptureJob(camera, request, guider)
        cameraCaptureJob.subscribe(messageService::sendMessage)
        register(jobLauncher.launch(cameraCaptureJob))
        return cameraCaptureJob.id
    }

    fun stop(camera: Camera) {
        findJobExecutionWithAny(camera)?.also { jobLauncher.stop(it) }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
