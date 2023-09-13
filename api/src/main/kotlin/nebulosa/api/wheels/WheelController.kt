package nebulosa.api.wheels

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("wheels")
class WheelController(
    private val connectionService: ConnectionService,
    private val wheelService: WheelService,
) {

    @GetMapping
    fun wheels(): List<FilterWheel> {
        return connectionService.wheels()
    }

    @GetMapping("{wheelName}")
    fun wheel(@RequestParam @Valid @NotBlank wheelName: String): FilterWheel {
        return requireNotNull(connectionService.wheel(wheelName))
    }

    @PutMapping("{wheelName}/connect")
    fun connect(@PathVariable wheelName: String) {
        wheelService.connect(wheel(wheelName))
    }

    @PutMapping("{wheelName}/disconnect")
    fun disconnect(@PathVariable wheelName: String) {
        wheelService.disconnect(wheel(wheelName))
    }

    @PutMapping("{wheelName}/move-to")
    fun moveTo(
        @PathVariable wheelName: String,
        @RequestParam @Valid @PositiveOrZero position: Int,
    ) {
        wheelService.moveTo(wheel(wheelName), position)
    }

    @PutMapping("{wheelName}/sync")
    fun sync(
        @PathVariable wheelName: String,
        @RequestParam @Valid @PositiveOrZero names: String,
    ) {
        wheelService.sync(wheel(wheelName), names.split(","))
    }
}
