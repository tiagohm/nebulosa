package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.data.responses.FilterWheelResponse
import nebulosa.api.services.FilterWheelService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FilterWheelController(
    private val filterWheelService: FilterWheelService,
) {

    @GetMapping("attachedFilterWheels")
    fun attachedFilterWheels(): List<FilterWheelResponse> {
        return filterWheelService.attachedFilterWheels()
    }

    @GetMapping("filterWheel")
    fun filterWheel(@RequestParam @Valid @NotBlank name: String): FilterWheelResponse {
        return filterWheelService[name]
    }

    @PostMapping("filterWheelConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        filterWheelService.connect(name)
    }

    @PostMapping("filterWheelDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        filterWheelService.disconnect(name)
    }

    @PostMapping("filterWheelMoveTo")
    fun moveTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero position: Int,
    ) {
        filterWheelService.moveTo(name, position)
    }

    @PostMapping("filterWheelSyncNames")
    fun syncNames(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero filterNames: String,
    ) {
        filterWheelService.syncNames(name, filterNames.split(","))
    }
}
