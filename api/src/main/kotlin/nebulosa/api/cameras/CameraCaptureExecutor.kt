package nebulosa.api.cameras

import nebulosa.api.jobs.JobExecutor
import nebulosa.api.messages.MessageService
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

    @Synchronized
    fun execute(camera: Camera, request: CameraStartCaptureRequest) {
        check(camera.connected) { "camera is not connected" }
        check(findJobExecutionWithAny(camera) == null) { "Camera Capture job is already running" }

        LOG.info { "starting camera capture. camera=$camera, request=$request" }

        val cameraCaptureJob = CameraCaptureJob(camera, request, guider)
        cameraCaptureJob.subscribe(messageService::sendMessage)
        jobExecutions.add(jobLauncher.launch(cameraCaptureJob))
    }

    fun stop(camera: Camera) {
        stopWithAny(camera)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
