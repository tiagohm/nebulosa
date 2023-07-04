package nebulosa.api.services

import nebulosa.api.components.CameraManager
import nebulosa.api.data.entities.CameraPreference
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.repositories.CameraPreferenceRepository
import nebulosa.common.concurrency.DaemonThreadFactory
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val cameraManager: CameraManager,
    private val cameraPreferenceRepository: CameraPreferenceRepository,
    private val capturesDiretory: Path,
) {

    private val runningTask = AtomicReference<CameraExposureTask>()

    fun list(): List<CameraResponse> {
        return cameraManager.map(::CameraResponse)
    }

    operator fun get(name: String): CameraResponse {
        val camera = cameraManager.first { it.name == name }
        return CameraResponse(camera)
    }

    fun connect(name: String) {
        val camera = cameraManager.first { it.name == name }
        camera.connect()
    }

    fun disconnect(name: String) {
        val camera = cameraManager.first { it.name == name }
        camera.disconnect()
    }

    fun setpointTemperature(name: String, value: Double) {
        val camera = cameraManager.first { it.name == name }
        camera.temperature(value)
    }

    fun cooler(name: String, value: Boolean) {
        val camera = cameraManager.first { it.name == name }
        camera.cooler(value)
    }

    @Synchronized
    fun startCapture(name: String, data: CameraStartCaptureRequest) {
        if (runningTask.get() != null) return

        val camera = cameraManager.first { it.name == name }
        val preference = cameraPreferenceRepository[name]
        val autoSave = preference?.autoSave ?: false
        val savePath = preference?.savePath?.ifBlank { null }?.let(Path::of)
            ?.takeIf { it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDiretory", name).createDirectories()
        val autoSubFolderMode = preference?.autoSubFolderMode ?: AutoSubFolderMode.NOON
        val task = CameraExposureTask(camera, data, autoSave, savePath, autoSubFolderMode)
        cameraManager.registerDeviceEventHandler(task)
        val future = CompletableFuture.runAsync(task, CAMERA_EXECUTOR)
        runningTask.set(task)

        future.whenComplete { _, _ ->
            cameraManager.unregisterDeviceEventHandler(task)
            runningTask.set(null)
        }
    }

    fun abortCapture(name: String) {
        runningTask.get()?.abort()
    }

    fun savePreferences(name: String, entity: CameraPreference) {
        val preference = cameraPreferenceRepository[name]
        cameraPreferenceRepository.save(entity.copy(id = preference?.id ?: 0L, name = name))
    }

    fun loadPreferences(name: String): CameraPreference {
        return cameraPreferenceRepository[name] ?: CameraPreference(name = name)
    }

    companion object {

        @JvmStatic private val CAMERA_EXECUTOR = Executors.newSingleThreadExecutor(DaemonThreadFactory)
    }
}
