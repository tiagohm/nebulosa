package nebulosa.api.connection

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.core.Controller
import nebulosa.api.validators.enumOf
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.range

class ConnectionController(
    override val app: Javalin,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        app.get("connection", ::statuses)
        app.get("connection/{id}", ::status)
        app.put("connection", ::connect)
        app.delete("connection/{id}", ::disconnect)
    }

    private fun connect(ctx: Context) {
        val host = ctx.queryParam("host").notNullOrBlank()
        val port = ctx.queryParam("port").notNull().toInt().range(1, 65535)
        val type = ctx.queryParam("type").notNull().enumOf<ConnectionType>()

        connectionService.connect(host, port, type)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.disconnect(id)
    }

    private fun statuses(ctx: Context) {
        ctx.json(connectionService.connectionStatuses())
    }

    private fun status(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.connectionStatus(id)?.also(ctx::json)
    }
}
