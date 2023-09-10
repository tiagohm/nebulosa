package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds

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

        val savePath = startCapture.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDirectory", camera.name)

        cameraCaptureExecutor.execute(
            camera,
            startCapture.exposureInMicroseconds.microseconds, startCapture.exposureAmount, startCapture.exposureDelayInSeconds.seconds,
            startCapture.x, startCapture.y, startCapture.width, startCapture.height,
            startCapture.frameFormat, startCapture.frameType, startCapture.binX, startCapture.binY,
            startCapture.gain, startCapture.offset, startCapture.autoSave, startCapture.autoSubFolderMode.pathFor(savePath).createDirectories(),
        )
    }

    fun abortCapture(camera: Camera) {
        cameraCaptureExecutor.stop(camera)
    }
}
