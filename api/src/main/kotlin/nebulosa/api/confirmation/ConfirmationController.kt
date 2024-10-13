package nebulosa.api.confirmation

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.pathParamAsClass
import nebulosa.api.core.Controller
import nebulosa.api.validators.notNull

class ConfirmationController(
    override val app: Javalin,
    private val confirmationService: ConfirmationService
) : Controller {

    init {
        app.put("confirmation/{idempotencyKey}", ::confirm)
    }

    private fun confirm(ctx: Context) {
        val idempotencyKey = ctx.pathParamAsClass<String>("idempotencyKey").get()
        val accepted = ctx.queryParam("accepted").notNull().toBoolean()
        confirmationService.confirm(idempotencyKey, accepted)
    }
}
