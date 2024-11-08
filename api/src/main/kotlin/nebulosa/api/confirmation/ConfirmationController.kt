package nebulosa.api.confirmation

import io.ktor.server.application.Application
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank

class ConfirmationController(
    override val server: Application,
    private val confirmationService: ConfirmationService
) : Controller {

    init {
        with(server) {
            routing {
                put("/confirmation/{idempotencyKey}", ::confirm)
            }
        }
    }

    private fun confirm(ctx: RoutingContext) = with(ctx.call) {
        val idempotencyKey = pathParameters["idempotencyKey"].notNullOrBlank()
        val accepted = queryParameters["accepted"].notNull().toBoolean()
        confirmationService.confirm(idempotencyKey, accepted)
    }
}
