package nebulosa.api.cameras

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.camera.Camera
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
class CameraController(
    private val connectionService: ConnectionService,
    private val cameraService: CameraService,
    private val cameraCaptureExecutor: CameraCaptureExecutor,
) {

    @GetMapping("attachedCameras")
    fun attachedCameras(): List<Camera> {
        return connectionService.cameras()
    }

    @GetMapping("camera")
    fun camera(@RequestParam @Valid @NotBlank name: String): Camera {
        return requireNotNull(connectionService.camera(name))
    }

    @PostMapping("cameraConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val camera = requireNotNull(connectionService.camera(name))
        cameraService.connect(camera)
    }

    @PostMapping("cameraDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val camera = requireNotNull(connectionService.camera(name))
        cameraService.disconnect(camera)
    }

    @GetMapping("cameraIsCapturing")
    fun isCapturing(@RequestParam @Valid @NotBlank name: String): Boolean {
        val camera = requireNotNull(connectionService.camera(name))
        return cameraService.isCapturing(camera)
    }

    @PostMapping("cameraSetpointTemperature")
    fun setpointTemperature(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @Range(min = -50, max = 50) temperature: Double
    ) {
        val camera = requireNotNull(connectionService.camera(name))
        cameraService.setpointTemperature(camera, temperature)
    }

    @PostMapping("cameraCooler")
    fun cooler(@RequestParam @Valid @NotBlank name: String, @RequestParam value: Boolean) {
        val camera = requireNotNull(connectionService.camera(name))
        cameraService.cooler(camera, value)
    }

    @PostMapping("cameraStartCapture")
    fun startCapture(@RequestParam @Valid @NotBlank name: String, @RequestBody @Valid body: CameraStartCaptureRequest) {
        val camera = requireNotNull(connectionService.camera(name))
        cameraService.startCapture(camera, body)
    }

    @PostMapping("cameraAbortCapture")
    fun abortCapture(@RequestParam @Valid @NotBlank name: String) {
        val camera = requireNotNull(connectionService.camera(name))
        cameraService.abortCapture(camera)
    }
}
