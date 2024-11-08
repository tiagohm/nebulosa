package nebulosa.api.connection

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.enumOf
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.range

class ConnectionController(
    override val server: Application,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/connections", ::statuses)
                get("/connections/{id}", ::status)
                put("/connections", ::connect)
                delete("/connections/{id}", ::disconnect)
            }
        }
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val host = queryParameters[HOST].notNullOrBlank()
        val port = queryParameters[PORT].notNull().toInt().range(1, 65535)
        val type = queryParameters[TYPE].notNull().enumOf<ConnectionType>()
        connectionService.connect(host, port, type)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID] ?: return
        connectionService.disconnect(id)
    }

    private suspend fun statuses(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.connectionStatuses())
    }

    private suspend fun status(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.connectionStatus(id))
    }

    companion object {

        private const val HOST = "host"
        private const val PORT = "port"
        private const val TYPE = "type"
        private const val ID = "id"
    }
}
