package nebulosa.api.wheels

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.beans.converters.device.DeviceOrEntityParam
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

    @GetMapping("{wheel}")
    fun wheel(@DeviceOrEntityParam wheel: FilterWheel): FilterWheel {
        return wheel
    }

    @PutMapping("{wheel}/connect")
    fun connect(@DeviceOrEntityParam wheel: FilterWheel) {
        wheelService.connect(wheel)
    }

    @PutMapping("{wheel}/disconnect")
    fun disconnect(@DeviceOrEntityParam wheel: FilterWheel) {
        wheelService.disconnect(wheel)
    }

    @PutMapping("{wheel}/move-to")
    fun moveTo(
        @DeviceOrEntityParam wheel: FilterWheel,
        @RequestParam @Valid @PositiveOrZero position: Int,
    ) {
        wheelService.moveTo(wheel, position)
    }

    @PutMapping("{wheel}/sync")
    fun sync(
        @DeviceOrEntityParam wheel: FilterWheel,
        @RequestParam @Valid @NotEmpty names: String,
    ) {
        wheelService.sync(wheel, names.split(","))
    }
}
