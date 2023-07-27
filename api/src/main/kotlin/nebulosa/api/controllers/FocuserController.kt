package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.data.responses.FocuserResponse
import nebulosa.api.services.FocuserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FocuserController(
    private val focuserService: FocuserService,
) {

    @GetMapping("attachedFocusers")
    fun attachedFocusers(): List<FocuserResponse> {
        return focuserService.attachedFocusers()
    }

    @GetMapping("focuser")
    fun focuser(@RequestParam @Valid @NotBlank name: String): FocuserResponse {
        return focuserService[name]
    }

    @PostMapping("focuserConnect")
    fun connect(@RequestParam @Valid @NotBlank name: String) {
        focuserService.connect(name)
    }

    @PostMapping("focuserDisconnect")
    fun disconnect(@RequestParam @Valid @NotBlank name: String) {
        focuserService.disconnect(name)
    }

    @PostMapping("focuserMoveIn")
    fun moveIn(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveIn(name, steps)
    }

    @PostMapping("focuserMoveOut")
    fun moveOut(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveOut(name, steps)
    }

    @PostMapping("focuserMoveTo")
    fun moveTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.moveTo(name, steps)
    }

    @PostMapping("focuserAbort")
    fun abort(@RequestParam @Valid @NotBlank name: String) {
        focuserService.abort(name)
    }

    @PostMapping("focuserSyncTo")
    fun syncTo(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @PositiveOrZero steps: Int,
    ) {
        focuserService.syncTo(name, steps)
    }
}
