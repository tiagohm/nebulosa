package nebulosa.api.confirmation

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.pathParamAsClass
import nebulosa.api.javalin.queryParamAsBoolean

class ConfirmationController(
    app: Javalin,
    private val confirmationService: ConfirmationService
) {

    init {
        app.put("confirmation/{idempotencyKey}", ::confirm)
    }

    private fun confirm(ctx: Context) {
        val idempotencyKey = ctx.pathParamAsClass<String>("idempotencyKey").get()
        val accepted = ctx.queryParamAsBoolean("accepted").get()
        confirmationService.confirm(idempotencyKey, accepted)
    }
}
