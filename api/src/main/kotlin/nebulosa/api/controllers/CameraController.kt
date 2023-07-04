package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.data.entities.CameraPreference
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.services.CameraService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("cameras")
class CameraController(
    private val cameraService: CameraService,
) {

    @GetMapping
    fun cameras(): List<CameraResponse> {
        return cameraService.list()
    }

    @GetMapping("{name}")
    fun camera(@PathVariable name: String): CameraResponse {
        return cameraService[name]
    }

    @PostMapping("{name}/connect")
    fun connect(@PathVariable name: String) {
        cameraService.connect(name)
    }

    @PostMapping("{name}/disconnect")
    fun disconnect(@PathVariable name: String) {
        cameraService.disconnect(name)
    }

    @PostMapping("{name}/setpointTemperature/{value}")
    fun setpointTemperature(@PathVariable name: String, @PathVariable value: Double) {
        cameraService.setpointTemperature(name, value)
    }

    @PostMapping("{name}/cooler/{value}")
    fun cooler(@PathVariable name: String, @PathVariable value: Boolean) {
        cameraService.cooler(name, value)
    }

    @PostMapping("{name}/capture/start")
    fun startCapture(@PathVariable name: String, @RequestBody @Valid body: CameraStartCaptureRequest) {
        cameraService.startCapture(name, body)
    }

    @PostMapping("{name}/capture/abort")
    fun abortCapture(@PathVariable name: String) {
        cameraService.abortCapture(name)
    }

    @PutMapping("{name}/preferences")
    fun savePreferences(@PathVariable name: String, @RequestBody body: CameraPreference) {
        cameraService.savePreferences(name, body)
    }

    @GetMapping("{name}/preferences")
    fun loadPreferences(@PathVariable name: String): CameraPreference {
        return cameraService.loadPreferences(name)
    }
}
