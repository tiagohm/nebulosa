package nebulosa.api.focusers

import jakarta.validation.Valid
import jakarta.validation.constraints.PositiveOrZero
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

    @GetMapping("{focuserName}")
    fun focuser(@PathVariable focuserName: String): Focuser {
        return requireNotNull(connectionService.focuser(focuserName))
    }

    @PutMapping("{focuserName}/connect")
    fun connect(@PathVariable focuserName: String) {
        focuserService.connect(focuser(focuserName))
    }

    @PutMapping("{focuserName}/disconnect")
    fun disconnect(@PathVariable focuserName: String) {
        focuserService.disconnect(focuser(focuserName))
    }

    @PutMapping("{focuserName}/move-in")
    fun moveIn(
        @PathVariable focuserName: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveIn(focuser(focuserName), steps)
    }

    @PutMapping("{focuserName}/move-out")
    fun moveOut(
        @PathVariable focuserName: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveOut(focuser(focuserName), steps)
    }

    @PutMapping("{focuserName}/move-to")
    fun moveTo(
        @PathVariable focuserName: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveTo(focuser(focuserName), steps)
    }

    @PutMapping("{focuserName}/abort")
    fun abort(@PathVariable focuserName: String) {
        focuserService.abort(focuser(focuserName))
    }

    @PutMapping("{focuserName}/sync")
    fun sync(
        @PathVariable focuserName: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.sync(focuser(focuserName), steps)
    }
}
