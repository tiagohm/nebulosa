package nebulosa.api.wheels

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WheelController(
    private val wheelService: WheelService,
) {

    @GetMapping("attachedWheels")
    fun attachedWheels(): List<FilterWheel> {
        return wheelService
    }

    @GetMapping("wheel")
    fun wheel(@RequestParam @Valid @NotBlank name: String): FilterWheel {
        return requireNotNull(wheelService[name])
    }

    @PostMapping("wheelConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val wheel = requireNotNull(wheelService[name])
        wheelService.connect(wheel)
    }

    @PostMapping("wheelDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val wheel = requireNotNull(wheelService[name])
        wheelService.disconnect(wheel)
    }

    @PostMapping("wheelMoveTo")
    fun moveTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero position: Int,
    ) {
        val wheel = requireNotNull(wheelService[name])
        wheelService.moveTo(wheel, position)
    }

    @PostMapping("wheelSyncNames")
    fun syncNames(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero filterNames: String,
    ) {
        val wheel = requireNotNull(wheelService[name])
        wheelService.syncNames(wheel, filterNames.split(","))
    }
}
