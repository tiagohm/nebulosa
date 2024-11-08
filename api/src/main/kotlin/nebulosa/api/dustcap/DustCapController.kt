package nebulosa.api.dustcap

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull

class DustCapController(
    override val app: Application,
    private val connectionService: ConnectionService,
    private val dustCapService: DustCapService,
) : Controller {

    init {
        with(app) {
            routing {
                get("/dust-caps", ::dustCaps)
                get("/dust-caps/{id}", ::dustCap)
                put("/dust-caps/{id}/connect", ::connect)
                put("/dust-caps/{id}/disconnect", ::disconnect)
                put("/dust-caps/{id}/park", ::park)
                put("/dust-caps/{id}/unpark", ::unpark)
                put("/dust-caps/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun dustCaps(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.dustCaps().sorted())
    }

    private suspend fun dustCap(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.dustCap(id))
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.connect(dustCap)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.disconnect(dustCap)
    }

    private fun park(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.park(dustCap)
    }

    private fun unpark(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.unpark(dustCap)
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.listen(dustCap)
    }

    companion object {

        private const val ID = "id"
    }
}
