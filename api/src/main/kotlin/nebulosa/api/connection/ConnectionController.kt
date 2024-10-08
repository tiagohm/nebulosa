package nebulosa.api.connection

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.javalin.queryParamAsInt
import nebulosa.api.javalin.queryParamAsString
import nebulosa.api.javalin.range

class ConnectionController(
    app: Javalin,
    private val connectionService: ConnectionService,
) {

    init {
        app.get("connection", ::statuses)
        app.get("connection/{id}", ::status)
        app.put("connection", ::connect)
        app.delete("connection/{id}", ::disconnect)
    }

    private fun connect(ctx: Context) {
        val host = ctx.queryParamAsString("host").get()
        val port = ctx.queryParamAsInt("port").range(PORT_RANGE).get()
        val type = ctx.queryParamAsString("type").getOrDefault("INDI").let(ConnectionType::valueOf)

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

    companion object {

        private val PORT_RANGE = 1..65535
    }
}
