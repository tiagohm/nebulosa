package nebulosa.api.controllers

import jakarta.validation.Valid
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.api.services.INDIService
import org.springframework.web.bind.annotation.*

@RestController
class INDIController(
    private val indiService: INDIService,
) {

    @GetMapping("indiProperties")
    fun properties(@RequestParam name: String): List<INDIPropertyResponse> {
        return indiService.properties(name)
    }

    @PostMapping("indiProperty")
    fun sendProperty(
        @RequestParam name: String,
        @RequestBody @Valid body: INDISendPropertyRequest,
    ) {
        return indiService.sendProperty(name, body)
    }
}
