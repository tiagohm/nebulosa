package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val capturesDirectory: Path,
    private val cameraExecutorService: ExecutorService,
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) {

    private val runningTasks = Collections.synchronizedMap(HashMap<Camera, CameraExposureTask>(2))

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onSavedCameraImageEvent(event: SavedCameraImageEntity) {
        event.id = savedCameraImageRepository.withPath(event.path)?.id ?: event.id
        savedCameraImageRepository.save(event)

        webSocketService.sendSavedCameraImageEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> webSocketService.sendCameraUpdated(event.device!!)
            is CameraCaptureFinished -> webSocketService.sendCameraCaptureFinished(event)
            is CameraAttached -> webSocketService.sendCameraAttached(event)
            is CameraDetached -> webSocketService.sendCameraDetached(event)
        }
    }

    fun connect(camera: Camera) {
        camera.connect()
    }

    fun disconnect(camera: Camera) {
        camera.disconnect()
    }

    fun isCapturing(camera: Camera): Boolean {
        return runningTasks.containsKey(camera)
    }

    fun setpointTemperature(camera: Camera, temperature: Double) {
        camera.temperature(temperature)
    }

    fun cooler(camera: Camera, enable: Boolean) {
        camera.cooler(enable)
    }

    @Synchronized
    fun startCapture(camera: Camera, data: CameraStartCaptureRequest) {
        if (isCapturing(camera)) return

        val savePath = data.savePath?.ifBlank { null }?.let(Path::of)
            ?.takeIf { it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDirectory", camera.name).createDirectories()

        val task = CameraExposureTask(camera, data, savePath)

        val future = CompletableFuture.runAsync(task, cameraExecutorService)
        runningTasks[camera] = task

        future.whenComplete { _, _ ->
            runningTasks.remove(camera)
        }
    }

    fun abortCapture(camera: Camera) {
        runningTasks[camera]?.abort()
    }
}
