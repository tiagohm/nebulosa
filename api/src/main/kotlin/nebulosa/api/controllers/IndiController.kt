package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.data.requests.DevicePropertyVectorRequest
import nebulosa.api.data.responses.DevicePropertyVectorResponse
import nebulosa.api.services.IndiService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("indi/{deviceName}")
class IndiController(
    private val indiService: IndiService,
) {

    @GetMapping("properties")
    fun properties(@PathVariable deviceName: String): List<DevicePropertyVectorResponse> {
        return indiService.properties(deviceName)
    }

    @PostMapping("properties")
    fun sendProperty(
        @PathVariable deviceName: String,
        @RequestBody @Valid body: DevicePropertyVectorRequest,
    ) {
        return indiService.sendProperty(deviceName, body)
    }
}
