package nebulosa.api.controllers

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
}
