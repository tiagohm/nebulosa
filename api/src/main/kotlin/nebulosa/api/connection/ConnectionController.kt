package nebulosa.api.connection

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.queryParamAsClass
import io.javalin.validation.Check

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
        val host = ctx.queryParamAsClass<String>("host").get()
        val port = ctx.queryParamAsClass<Int>("port").check(PortRangeCheck, "invalid port range").get()
        val type = ctx.queryParamAsClass<String>("type").getOrDefault("INDI").let(ConnectionType::valueOf)

        connectionService.connect(host, port, type)
    }

    fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.disconnect(id)
    }

    fun statuses(ctx: Context) {
        ctx.json(connectionService.connectionStatuses())
    }

    fun status(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.connectionStatus(id)?.also(ctx::json)
    }

    private object PortRangeCheck : Check<Int> {

        override fun invoke(port: Int): Boolean {
            return port in 1..65535
        }
    }
}
