package nebulosa.api.cameras

import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.api.services.MessageService
import nebulosa.indi.device.camera.Camera
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val capturesDirectory: Path,
    private val cameraCaptureExecutor: CameraCaptureExecutor,
    private val messageService: MessageService,
    private val eventBus: EventBus,
) {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onSavedCameraImageEvent(event: SavedCameraImageEntity) {
        event.id = savedCameraImageRepository.withPath(event.path)?.id ?: event.id
        savedCameraImageRepository.save(event)
        messageService.sendMessage(CAMERA_IMAGE_SAVED, event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraCaptureUpdated(event: CameraCaptureEvent) {
        when (event) {
            is CameraExposureUpdated -> messageService.sendMessage(CAMERA_EXPOSURE_UPDATED, event)
            is CameraCaptureFinished -> messageService.sendMessage(CAMERA_CAPTURE_FINISHED, event)
        }
    }

    fun connect(camera: Camera) {
        camera.connect()
    }

    fun disconnect(camera: Camera) {
        camera.disconnect()
    }

    fun isCapturing(camera: Camera): Boolean {
        return cameraCaptureExecutor.isCapturing(camera)
    }

    fun setpointTemperature(camera: Camera, temperature: Double) {
        camera.temperature(temperature)
    }

    fun cooler(camera: Camera, enable: Boolean) {
        camera.cooler(enable)
    }

    @Synchronized
    fun startCapture(camera: Camera, startCapture: CameraStartCaptureRequest) {
        if (isCapturing(camera)) return

        startCapture.savePath = startCapture.savePath
            ?.takeIf { it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDirectory", camera.name).createDirectories()

        cameraCaptureExecutor.execute(camera, startCapture)
    }

    fun abortCapture(camera: Camera) {
        cameraCaptureExecutor.stop(camera)
    }

    companion object {

        const val CAMERA_IMAGE_SAVED = "CAMERA_IMAGE_SAVED"
        const val CAMERA_EXPOSURE_UPDATED = "CAMERA_EXPOSURE_UPDATED"
        const val CAMERA_CAPTURE_FINISHED = "CAMERA_CAPTURE_FINISHED"
    }
}
