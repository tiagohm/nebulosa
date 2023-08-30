package nebulosa.api.indi

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.indi.device.PropertyVector
import org.springframework.web.bind.annotation.*

@RestController
class INDIController(
    private val indiService: INDIService,
) {

    @GetMapping("indiProperties")
    fun properties(@RequestParam @Valid @NotBlank name: String): Collection<PropertyVector<*, *>> {
        val device = requireNotNull(indiService[name])
        return indiService.properties(device)
    }

    @PostMapping("sendIndiProperty")
    fun sendProperty(
        @RequestParam @Valid @NotBlank name: String,
        @RequestBody @Valid body: INDISendPropertyRequest,
    ) {
        val device = requireNotNull(indiService[name])
        return indiService.sendProperty(device, body)
    }

    @GetMapping("indiLog")
    fun indiLog(@RequestParam(required = false) name: String?): List<String> {
        if (name.isNullOrBlank()) return indiService
        val device = indiService[name] ?: return emptyList()
        return device.messages
    }
}
