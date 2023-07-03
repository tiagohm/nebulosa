package nebulosa.api.controllers

import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.services.CameraService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("cameras")
class CameraController(
    private val cameraService: CameraService,
) {

    @GetMapping
    fun cameras(): List<CameraResponse> {
        return cameraService.list()
    }

    @GetMapping("{deviceName}")
    fun camera(@PathVariable deviceName: String): CameraResponse {
        return cameraService[deviceName]
    }
}
