package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.repositories.CameraPreferenceRepository
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.indi.device.PropertyChangedEvent
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
    private val equipmentService: EquipmentService,
    private val cameraPreferenceRepository: CameraPreferenceRepository,
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val capturesDirectory: Path,
    private val cameraExecutorService: ExecutorService,
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) {

    private val runningTasks = Collections.synchronizedMap(HashMap<String, CameraExposureTask>(2))

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
            is CameraCaptureFinished -> webSocketService.sendCameraCaptureFinished(event.device)
            is CameraAttached -> webSocketService.sendCameraAttached(event.device)
            is CameraDetached -> webSocketService.sendCameraDetached(event.device)
        }
    }

    fun attachedCameras(): List<CameraResponse> {
        return equipmentService.cameras().map(::CameraResponse)
    }

    operator fun get(name: String): CameraResponse {
        val camera = equipmentService.camera(name)!!
        return CameraResponse(camera)
    }

    fun connect(name: String) {
        val camera = equipmentService.camera(name)!!
        camera.connect()
    }

    fun disconnect(name: String) {
        val camera = equipmentService.camera(name)!!
        camera.disconnect()
    }

    fun isCapturing(name: String): Boolean {
        return runningTasks.containsKey(name)
    }

    fun setpointTemperature(name: String, temperature: Double) {
        val camera = equipmentService.camera(name)!!
        camera.temperature(temperature)
    }

    fun cooler(name: String, enable: Boolean) {
        val camera = equipmentService.camera(name)!!
        camera.cooler(enable)
    }

    @Synchronized
    fun startCapture(name: String, data: CameraStartCaptureRequest) {
        if (isCapturing(name)) return

        val camera = equipmentService.camera(name)!!
        val preference = cameraPreferenceRepository.withName(name)
        val autoSave = preference?.autoSave ?: false
        val savePath = preference?.savePath?.ifBlank { null }?.let(Path::of)
            ?.takeIf { it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDirectory", name).createDirectories()
        val autoSubFolderMode = preference?.autoSubFolderMode ?: AutoSubFolderMode.NOON

        val task = CameraExposureTask(camera, data, autoSave, savePath, autoSubFolderMode)

        val future = CompletableFuture.runAsync(task, cameraExecutorService)
        runningTasks[name] = task

        future.whenComplete { _, _ ->
            runningTasks.remove(name)
        }
    }

    fun abortCapture(name: String) {
        runningTasks[name]?.abort()
    }
}
