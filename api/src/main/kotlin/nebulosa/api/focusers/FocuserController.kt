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
        return connectionService.focusers().sorted()
    }

    @GetMapping("{focuser}")
    fun focuser(focuser: Focuser): Focuser {
        return focuser
    }

    @PutMapping("{focuser}/connect")
    fun connect(focuser: Focuser) {
        focuserService.connect(focuser)
    }

    @PutMapping("{focuser}/disconnect")
    fun disconnect(focuser: Focuser) {
        focuserService.disconnect(focuser)
    }

    @PutMapping("{focuser}/move-in")
    fun moveIn(
        focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveIn(focuser, steps)
    }

    @PutMapping("{focuser}/move-out")
    fun moveOut(
        focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveOut(focuser, steps)
    }

    @PutMapping("{focuser}/move-to")
    fun moveTo(
        focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveTo(focuser, steps)
    }

    @PutMapping("{focuser}/abort")
    fun abort(focuser: Focuser) {
        focuserService.abort(focuser)
    }

    @PutMapping("{focuser}/sync")
    fun sync(
        focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.sync(focuser, steps)
    }
}
