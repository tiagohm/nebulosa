package nebulosa.api.confirmation

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("confirmation")
class ConfirmationController(private val confirmationService: ConfirmationService) {

    @PutMapping("{idempotencyKey}")
    fun confirm(@PathVariable idempotencyKey: String, @RequestParam accepted: Boolean) {
        confirmationService.confirm(idempotencyKey, accepted)
    }
}
