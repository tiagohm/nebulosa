package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.services.CameraService
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
class CameraController(
    private val cameraService: CameraService,
) {

    @GetMapping("attachedCameras")
    fun attachedCameras(): List<CameraResponse> {
        return cameraService.attachedCameras()
    }

    @GetMapping("camera")
    fun camera(@RequestParam @Valid @NotBlank name: String): CameraResponse {
        return cameraService[name]
    }

    @PostMapping("cameraConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        cameraService.connect(name)
    }

    @PostMapping("cameraDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        cameraService.disconnect(name)
    }

    @GetMapping("cameraIsCapturing")
    fun isCapturing(@RequestParam @Valid @NotBlank name: String): Boolean {
        return cameraService.isCapturing(name)
    }

    @PostMapping("cameraSetpointTemperature")
    fun setpointTemperature(@RequestParam @Valid @NotBlank name: String, @RequestParam @Valid @Range(min = -50, max = 50) temperature: Double) {
        cameraService.setpointTemperature(name, temperature)
    }

    @PostMapping("cameraCooler")
    fun cooler(@RequestParam @Valid @NotBlank name: String, @RequestParam value: Boolean) {
        cameraService.cooler(name, value)
    }

    @PostMapping("cameraStartCapture")
    fun startCapture(@RequestParam @Valid @NotBlank name: String, @RequestBody @Valid body: CameraStartCaptureRequest) {
        cameraService.startCapture(name, body)
    }

    @PostMapping("cameraAbortCapture")
    fun abortCapture(@RequestParam @Valid @NotBlank name: String) {
        cameraService.abortCapture(name)
    }
}
