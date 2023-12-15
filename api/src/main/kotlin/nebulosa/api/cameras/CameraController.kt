package nebulosa.api.cameras

import jakarta.validation.Valid
import nebulosa.api.beans.annotations.EntityParam
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

    @GetMapping("{camera}")
    fun camera(@EntityParam camera: Camera): Camera {
        return camera
    }

    @PutMapping("{camera}/connect")
    fun connect(@EntityParam camera: Camera) {
        cameraService.connect(camera)
    }

    @PutMapping("{camera}/disconnect")
    fun disconnect(@EntityParam camera: Camera) {
        cameraService.disconnect(camera)
    }

    @GetMapping("{camera}/capturing")
    fun isCapturing(@EntityParam camera: Camera): Boolean {
        return cameraService.isCapturing(camera)
    }

    @PutMapping("{camera}/cooler")
    fun cooler(
        @EntityParam camera: Camera,
        @RequestParam enabled: Boolean,
    ) {
        cameraService.cooler(camera, enabled)
    }

    @PutMapping("{camera}/temperature/setpoint")
    fun setpointTemperature(
        @EntityParam camera: Camera,
        @RequestParam @Valid @Range(min = -50, max = 50) temperature: Double,
    ) {
        cameraService.setpointTemperature(camera, temperature)
    }

    @PutMapping("{camera}/capture/start")
    fun startCapture(
        @EntityParam camera: Camera,
        @RequestBody body: CameraStartCaptureRequest,
    ) {
        cameraService.startCapture(camera, body)
    }

    @PutMapping("{camera}/capture/abort")
    fun abortCapture(@EntityParam camera: Camera) {
        cameraService.abortCapture(camera)
    }
}
