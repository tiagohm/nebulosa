package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val capturesPath: Path,
    private val cameraCaptureExecutor: CameraCaptureExecutor,
) {

    fun connect(camera: Camera) {
        camera.connect()
    }

    fun disconnect(camera: Camera) {
        camera.disconnect()
    }

    fun setpointTemperature(camera: Camera, temperature: Double) {
        camera.temperature(temperature)
    }

    fun cooler(camera: Camera, enabled: Boolean) {
        camera.cooler(enabled)
    }

    @Synchronized
    fun startCapture(camera: Camera, request: CameraStartCaptureRequest) {
        val savePath = request.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesPath", camera.name, request.frameType.name)

        cameraCaptureExecutor
            .execute(camera, request.copy(savePath = savePath))
    }

    fun abortCapture(camera: Camera) {
        cameraCaptureExecutor.stop(camera)
    }
}
