package nebulosa.api.cameras

import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class CameraService(
    private val capturesPath: Path,
    private val cameraCaptureExecutor: CameraCaptureExecutor,
    private val cameraEventHub: CameraEventHub,
) {

    fun connect(camera: Camera) {
        camera.connect()
    }

    fun disconnect(camera: Camera) {
        camera.disconnect()
    }

    fun snoop(camera: Camera, vararg devices: Device?) {
        camera.snoop(devices.toList())
    }

    fun setpointTemperature(camera: Camera, temperature: Double) {
        camera.temperature(temperature)
    }

    fun cooler(camera: Camera, enabled: Boolean) {
        camera.cooler(enabled)
    }

    @Synchronized
    fun startCapture(
        camera: Camera, request: CameraStartCaptureRequest,
        mount: Mount? = null, wheel: FilterWheel? = null, focuser: Focuser? = null, rotator: Rotator? = null
    ) {
        val savePath = request.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesPath", camera.name, request.frameType.name)

        cameraCaptureExecutor.execute(camera, request.copy(savePath = savePath), mount, wheel, focuser, rotator)
    }

    fun pauseCapture(camera: Camera) {
        cameraCaptureExecutor.pause(camera)
    }

    fun unpauseCapture(camera: Camera) {
        cameraCaptureExecutor.unpause(camera)
    }

    @Synchronized
    fun abortCapture(camera: Camera) {
        cameraCaptureExecutor.stop(camera)
    }

    fun captureStatus(camera: Camera): CameraCaptureEvent? {
        return cameraCaptureExecutor.status(camera)
    }

    fun listen(camera: Camera) {
        cameraEventHub.listen(camera)
    }
}
