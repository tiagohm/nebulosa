package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.data.entities.CameraPreference
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.services.CameraService
import org.springframework.web.bind.annotation.*

@RestController
class CameraController(
    private val cameraService: CameraService,
) {

    @GetMapping("attachedCameras")
    fun sttachedCameras(): List<CameraResponse> {
        return cameraService.attachedCameras()
    }

    @GetMapping("camera")
    fun camera(@RequestParam name: String): CameraResponse {
        return cameraService[name]
    }

    @PostMapping("cameraConnect")
    fun connect(@RequestParam name: String) {
        cameraService.connect(name)
    }

    @PostMapping("cameraDisconnect")
    fun disconnect(@RequestParam name: String) {
        cameraService.disconnect(name)
    }

    @GetMapping("cameraIsCapturing")
    fun isCapturing(@RequestParam name: String): Boolean {
        return cameraService.isCapturing(name)
    }

    @PostMapping("cameraSetpointTemperature")
    fun setpointTemperature(@RequestParam name: String, @RequestParam temperature: Double) {
        cameraService.setpointTemperature(name, temperature)
    }

    @PostMapping("cameraCooler")
    fun cooler(@RequestParam name: String, @RequestParam value: Boolean) {
        cameraService.cooler(name, value)
    }

    @PostMapping("cameraStartCapture")
    fun startCapture(@RequestParam name: String, @RequestBody @Valid body: CameraStartCaptureRequest) {
        cameraService.startCapture(name, body)
    }

    @PostMapping("cameraAbortCapture")
    fun abortCapture(@RequestParam name: String) {
        cameraService.abortCapture(name)
    }

    @PutMapping("cameraPreferences")
    fun savePreferences(@RequestParam name: String, @RequestBody @Valid body: CameraPreference) {
        cameraService.savePreferences(body.also { it.name = name })
    }

    @GetMapping("cameraPreferences")
    fun loadPreferences(@RequestParam name: String): CameraPreference {
        return cameraService.loadPreferences(name)
    }
}
