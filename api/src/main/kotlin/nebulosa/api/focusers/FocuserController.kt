package nebulosa.api.focusers

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.positiveOrZero

class FocuserController(
    override val server: Application,
    private val connectionService: ConnectionService,
    private val focuserService: FocuserService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/focusers", ::focusers)
                get("/focusers/{id}", ::focuser)
                put("/focusers/{id}/connect", ::connect)
                put("/focusers/{id}/disconnect", ::disconnect)
                put("/focusers/{id}/move-in", ::moveIn)
                put("/focusers/{id}/move-out", ::moveOut)
                put("/focusers/{id}/move-to", ::moveTo)
                put("/focusers/{id}/abort", ::abort)
                put("/focusers/{id}/sync", ::sync)
                put("/focusers/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun focusers(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.focusers().sorted())
    }

    private suspend fun focuser(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        respond(focuser)
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        focuserService.connect(focuser)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        focuserService.disconnect(focuser)
    }

    private fun moveIn(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        val steps = queryParameters["steps"].notNull().toInt().positiveOrZero()
        focuserService.moveIn(focuser, steps)
    }

    private fun moveOut(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        val steps = queryParameters["steps"].notNull().toInt().positiveOrZero()
        focuserService.moveOut(focuser, steps)
    }

    private fun moveTo(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        val steps = queryParameters["steps"].notNull().toInt().positiveOrZero()
        focuserService.moveTo(focuser, steps)
    }

    private fun abort(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        focuserService.abort(focuser)
    }

    private fun sync(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        val steps = queryParameters["steps"].notNull().toInt().positiveOrZero()
        focuserService.sync(focuser, steps)
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val focuser = connectionService.focuser(id) ?: return
        focuserService.listen(focuser)
    }

    companion object {

        private const val ID = "id"
    }
}
