package nebulosa.api.confirmation

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.http.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank

class ConfirmationController(
    override val app: Javalin,
    private val confirmationService: ConfirmationService
) : Controller {

    init {
        app.put("confirmation/{idempotencyKey}", ::confirm)
    }

    private fun confirm(ctx: Context) {
        val idempotencyKey = ctx.pathParam("idempotencyKey").notNullOrBlank()
        val accepted = ctx.queryParam("accepted").notNull().toBoolean()
        confirmationService.confirm(idempotencyKey, accepted)
    }
}
