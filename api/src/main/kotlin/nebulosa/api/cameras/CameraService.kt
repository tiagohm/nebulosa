package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val capturesDirectory: Path,
    private val cameraCaptureExecutor: CameraCaptureExecutor,
) {

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
}
