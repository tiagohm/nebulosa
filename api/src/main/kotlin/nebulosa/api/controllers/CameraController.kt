package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.services.CameraService
import nebulosa.api.services.EquipmentService
import nebulosa.indi.device.camera.Camera
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
class CameraController(
    private val equipmentService: EquipmentService,
    private val cameraService: CameraService,
) {

    @GetMapping("attachedCameras")
    fun attachedCameras(): List<Camera> {
        return equipmentService.cameras()
    }

    @GetMapping("camera")
    fun camera(@RequestParam @Valid @NotBlank name: String): Camera {
        return requireNotNull(equipmentService.camera(name))
    }

    @PostMapping("cameraConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val camera = requireNotNull(equipmentService.camera(name))
        cameraService.connect(camera)
    }

    @PostMapping("cameraDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val camera = requireNotNull(equipmentService.camera(name))
        cameraService.disconnect(camera)
    }

    @GetMapping("cameraIsCapturing")
    fun isCapturing(@RequestParam @Valid @NotBlank name: String): Boolean {
        val camera = requireNotNull(equipmentService.camera(name))
        return cameraService.isCapturing(camera)
    }

    @PostMapping("cameraSetpointTemperature")
    fun setpointTemperature(@RequestParam @Valid @NotBlank name: String, @RequestParam @Valid @Range(min = -50, max = 50) temperature: Double) {
        val camera = requireNotNull(equipmentService.camera(name))
        cameraService.setpointTemperature(camera, temperature)
    }

    @PostMapping("cameraCooler")
    fun cooler(@RequestParam @Valid @NotBlank name: String, @RequestParam value: Boolean) {
        val camera = requireNotNull(equipmentService.camera(name))
        cameraService.cooler(camera, value)
    }

    @PostMapping("cameraStartCapture")
    fun startCapture(@RequestParam @Valid @NotBlank name: String, @RequestBody @Valid body: CameraStartCaptureRequest) {
        val camera = requireNotNull(equipmentService.camera(name))
        cameraService.startCapture(camera, body)
    }

    @PostMapping("cameraAbortCapture")
    fun abortCapture(@RequestParam @Valid @NotBlank name: String) {
        val camera = requireNotNull(equipmentService.camera(name))
        cameraService.abortCapture(camera)
    }
}
