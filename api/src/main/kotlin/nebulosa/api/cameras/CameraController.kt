package nebulosa.api.cameras

import jakarta.validation.Valid
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.camera.Camera
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("cameras")
class CameraController(
    private val connectionService: ConnectionService,
    private val cameraService: CameraService,
) {

    @GetMapping
    fun cameras(): List<Camera> {
        return connectionService.cameras()
    }

    @GetMapping("{cameraName}")
    fun camera(@PathVariable cameraName: String): Camera {
        return requireNotNull(connectionService.camera(cameraName))
    }

    @PutMapping("{cameraName}/connect")
    fun connect(@PathVariable cameraName: String) {
        cameraService.connect(camera(cameraName))
    }

    @PutMapping("{cameraName}/disconnect")
    fun disconnect(@PathVariable cameraName: String) {
        cameraService.disconnect(camera(cameraName))
    }

    @GetMapping("{cameraName}/capturing")
    fun isCapturing(@PathVariable cameraName: String): Boolean {
        return cameraService.isCapturing(camera(cameraName))
    }

    @PutMapping("{cameraName}/cooler")
    fun cooler(
        @PathVariable cameraName: String,
        @RequestParam enabled: Boolean,
    ) {
        cameraService.cooler(camera(cameraName), enabled)
    }

    @PutMapping("{cameraName}/temperature/setpoint")
    fun setpointTemperature(
        @PathVariable cameraName: String,
        @RequestParam @Valid @Range(min = -50, max = 50) temperature: Double,
    ) {
        cameraService.setpointTemperature(camera(cameraName), temperature)
    }

    @PutMapping("{cameraName}/capture/start")
    fun startCapture(
        @PathVariable cameraName: String,
        @RequestBody body: CameraCaptureRequest,
    ) {
        val camera = camera(cameraName)
        cameraService.startCapture(camera, body)
    }

    @PutMapping("{cameraName}/capture/abort")
    fun abortCapture(@PathVariable cameraName: String) {
        cameraService.abortCapture(camera(cameraName))
    }
}
