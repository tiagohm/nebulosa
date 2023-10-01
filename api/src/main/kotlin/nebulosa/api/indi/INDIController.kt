package nebulosa.api.indi

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.PropertyVector
import org.springframework.web.bind.annotation.*

@RestController
class INDIController(
    private val connectionService: ConnectionService,
    private val indiService: INDIService,
) {

    @GetMapping("indiProperties")
    fun properties(@RequestParam @Valid @NotBlank name: String): Collection<PropertyVector<*, *>> {
        val device = requireNotNull(connectionService.device(name))
        return indiService.properties(device)
    }

    @PostMapping("sendIndiProperty")
    fun sendProperty(
        @RequestParam @Valid @NotBlank name: String,
        @RequestBody @Valid body: INDISendProperty,
    ) {
        val device = requireNotNull(connectionService.device(name))
        return indiService.sendProperty(device, body)
    }

    @GetMapping("indiLog")
    fun indiLog(@RequestParam(required = false) name: String?): List<String> {
        if (name.isNullOrBlank()) return indiService.messages()
        val device = connectionService.device(name) ?: return emptyList()
        return device.messages
    }
}
