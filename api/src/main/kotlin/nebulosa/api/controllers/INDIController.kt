package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.services.EquipmentService
import nebulosa.api.services.INDIService
import nebulosa.api.services.WebSocketService
import nebulosa.indi.device.PropertyVector
import org.springframework.web.bind.annotation.*

@RestController
class INDIController(
    private val equipmentService: EquipmentService,
    private val indiService: INDIService,
    private val webSocketService: WebSocketService,
) {

    @GetMapping("indiProperties")
    fun properties(@RequestParam @Valid @NotBlank name: String): Collection<PropertyVector<*, *>> {
        val device = requireNotNull(equipmentService[name])
        return indiService.properties(device)
    }

    @PostMapping("sendIndiProperty")
    fun sendProperty(
        @RequestParam @Valid @NotBlank name: String,
        @RequestBody @Valid body: INDISendPropertyRequest,
    ) {
        val device = requireNotNull(equipmentService[name])
        return indiService.sendProperty(device, body)
    }

    @GetMapping("indiLog")
    fun indiLog(@RequestParam(required = false) name: String?): List<String> {
        if (name.isNullOrBlank()) return indiService
        val device = equipmentService[name] ?: return emptyList()
        return device.messages
    }

    @PostMapping("indiStartListening")
    fun indiStartListening() {
        return webSocketService.indiStartListening()
    }

    @PostMapping("indiStopListening")
    fun indiStopListening() {
        return webSocketService.indiStopListening()
    }
}
