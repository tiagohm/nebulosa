package nebulosa.api.services

import nebulosa.api.data.entities.CameraPreference
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.repositories.CameraPreferenceRepository
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.common.concurrency.DaemonThreadFactory
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val equipmentService: EquipmentService,
    private val cameraPreferenceRepository: CameraPreferenceRepository,
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val capturesDiretory: Path,
) {

    private val runningTasks = Collections.synchronizedMap(HashMap<String, CameraExposureTask>(2))

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
        val preference = cameraPreferenceRepository.findName(name)
        val autoSave = preference?.autoSave ?: false
        val savePath = preference?.savePath?.ifBlank { null }?.let(Path::of)
            ?.takeIf { it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDiretory", name).createDirectories()
        val autoSubFolderMode = preference?.autoSubFolderMode ?: AutoSubFolderMode.NOON

        val task = CameraExposureTask(camera, data, autoSave, savePath, autoSubFolderMode) {
            it.id = savedCameraImageRepository.withPath(it.path)?.id ?: it.id
            savedCameraImageRepository.save(it)
        }

        equipmentService.registerDeviceEventHandler(task)
        val future = CompletableFuture.runAsync(task, CAMERA_EXECUTOR)
        runningTasks[name] = task

        future.whenComplete { _, _ ->
            equipmentService.unregisterDeviceEventHandler(task)
            runningTasks.remove(name)
        }
    }

    fun abortCapture(name: String) {
        runningTasks[name]?.abort()
    }

    fun savePreferences(entity: CameraPreference) {
        val preference = cameraPreferenceRepository.findName(entity.name)
        cameraPreferenceRepository.save(entity.copy(id = preference?.id ?: 0L))
    }

    fun loadPreferences(name: String): CameraPreference {
        return cameraPreferenceRepository.findName(name) ?: CameraPreference(name = name)
    }

    companion object {

        @JvmStatic private val CAMERA_EXECUTOR = Executors.newSingleThreadExecutor(DaemonThreadFactory)
    }
}
