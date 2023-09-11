package nebulosa.api.cameras

import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.time.Duration

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

    fun cooler(camera: Camera, enabled: Boolean) {
        camera.cooler(enabled)
    }

    @Synchronized
    fun startCapture(
        camera: Camera,
        exposureTime: Duration = Duration.ZERO,
        exposureAmount: Int = 1,
        exposureDelay: Duration = Duration.ZERO,
        x: Int = camera.minX, y: Int = camera.minY,
        width: Int = camera.maxWidth, height: Int = camera.maxHeight,
        frameFormat: String? = null,
        frameType: FrameType = FrameType.LIGHT,
        binX: Int = camera.binX, binY: Int = binX,
        gain: Int = camera.gain, offset: Int = camera.offset,
        autoSave: Boolean = false, savePath: Path? = null,
        autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
    ) {
        if (isCapturing(camera)) return

        val savePath = savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesDirectory", camera.name)

        cameraCaptureExecutor.execute(
            camera,
            exposureTime, exposureAmount, exposureDelay,
            x, y, width, height,
            frameFormat, frameType, binX, binY,
            gain, offset, autoSave, autoSubFolderMode.pathFor(savePath).createDirectories(),
        )
    }

    fun abortCapture(camera: Camera) {
        cameraCaptureExecutor.stop(camera)
    }
}
