package nebulosa.api.focusers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.focuser.Focuser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FocuserController(
    private val connectionService: ConnectionService,
    private val focuserService: FocuserService,
) {

    @GetMapping("attachedFocusers")
    fun attachedFocusers(): List<Focuser> {
        return connectionService.focusers()
    }

    @GetMapping("focuser")
    fun focuser(@RequestParam @Valid @NotBlank name: String): Focuser {
        return requireNotNull(connectionService.focuser(name))
    }

    @PostMapping("focuserConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.connect(focuser)
    }

    @PostMapping("focuserDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.disconnect(focuser)
    }

    @PostMapping("focuserMoveIn")
    fun moveIn(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.moveIn(focuser, steps)
    }

    @PostMapping("focuserMoveOut")
    fun moveOut(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.moveOut(focuser, steps)
    }

    @PostMapping("focuserMoveTo")
    fun moveTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.moveTo(focuser, steps)
    }

    @PostMapping("focuserAbort")
    fun abort(@RequestParam @Valid @NotBlank name: String) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.abort(focuser)
    }

    @PostMapping("focuserSyncTo")
    fun syncTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        val focuser = requireNotNull(connectionService.focuser(name))
        focuserService.syncTo(focuser, steps)
    }
}
