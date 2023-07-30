package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.data.responses.FilterWheelResponse
import nebulosa.api.services.EquipmentService
import nebulosa.api.services.FilterWheelService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FilterWheelController(
    private val equipmentService: EquipmentService,
    private val filterWheelService: FilterWheelService,
) {

    @GetMapping("attachedFilterWheels")
    fun attachedFilterWheels(): List<FilterWheelResponse> {
        return equipmentService.filterWheels().map(::FilterWheelResponse)
    }

    @GetMapping("filterWheel")
    fun filterWheel(@RequestParam @Valid @NotBlank name: String): FilterWheelResponse {
        return FilterWheelResponse(requireNotNull(equipmentService.filterWheel(name)))
    }

    @PostMapping("filterWheelConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheelService.connect(filterWheel)
    }

    @PostMapping("filterWheelDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheelService.disconnect(filterWheel)
    }

    @PostMapping("filterWheelMoveTo")
    fun moveTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero position: Int,
    ) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheelService.moveTo(filterWheel, position)
    }

    @PostMapping("filterWheelSyncNames")
    fun syncNames(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero filterNames: String,
    ) {
        val filterWheel = requireNotNull(equipmentService.filterWheel(name))
        filterWheelService.syncNames(filterWheel, filterNames.split(","))
    }
}
