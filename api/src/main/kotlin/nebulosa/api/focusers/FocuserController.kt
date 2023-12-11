package nebulosa.api.focusers

import jakarta.validation.Valid
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.beans.annotations.EntityParam
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
    fun focuser(@EntityParam focuser: Focuser): Focuser {
        return focuser
    }

    @PutMapping("{focuser}/connect")
    fun connect(@EntityParam focuser: Focuser) {
        focuserService.connect(focuser)
    }

    @PutMapping("{focuser}/disconnect")
    fun disconnect(@EntityParam focuser: Focuser) {
        focuserService.disconnect(focuser)
    }

    @PutMapping("{focuser}/move-in")
    fun moveIn(
        @EntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveIn(focuser, steps)
    }

    @PutMapping("{focuser}/move-out")
    fun moveOut(
        @EntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveOut(focuser, steps)
    }

    @PutMapping("{focuser}/move-to")
    fun moveTo(
        @EntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveTo(focuser, steps)
    }

    @PutMapping("{focuser}/abort")
    fun abort(@EntityParam focuser: Focuser) {
        focuserService.abort(focuser)
    }

    @PutMapping("{focuser}/sync")
    fun sync(
        @EntityParam focuser: Focuser,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.sync(focuser, steps)
    }
}
