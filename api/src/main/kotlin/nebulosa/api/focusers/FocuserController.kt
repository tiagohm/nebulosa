package nebulosa.api.focusers

import jakarta.validation.Valid
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.beans.converters.device.DeviceOrEntityParam
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.focuser.Focuser
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("focusers")
class FocuserController(
    private val connectionService: ConnectionService,
    private val focuserService: FocuserService,
) {

    @GetMapping
    fun focusers(): List<Focuser> {
        return connectionService.focusers()
    }

    @GetMapping("{focuser}")
    fun focuser(@DeviceOrEntityParam focuser: Focuser): Focuser {
        return focuser
    }

    @PutMapping("{focuser}/connect")
    fun connect(@DeviceOrEntityParam focuser: Focuser) {
        focuserService.connect(focuser)
    }

    @PutMapping("{focuser}/disconnect")
    fun disconnect(@DeviceOrEntityParam focuser: Focuser) {
        focuserService.disconnect(focuser)
    }

    @PutMapping("{focuser}/move-in")
    fun moveIn(
        @DeviceOrEntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveIn(focuser, steps)
    }

    @PutMapping("{focuser}/move-out")
    fun moveOut(
        @DeviceOrEntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveOut(focuser, steps)
    }

    @PutMapping("{focuser}/move-to")
    fun moveTo(
        @DeviceOrEntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveTo(focuser, steps)
    }

    @PutMapping("{focuser}/abort")
    fun abort(@DeviceOrEntityParam focuser: Focuser) {
        focuserService.abort(focuser)
    }

    @PutMapping("{focuser}/sync")
    fun sync(
        @DeviceOrEntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.sync(focuser, steps)
    }
}
