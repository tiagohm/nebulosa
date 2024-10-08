package nebulosa.api.dustcap

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService

class DustCapController(
    app: Javalin,
    private val connectionService: ConnectionService,
    private val dustCapService: DustCapService,
) {

    init {
        app.get("dust-caps", ::dustCaps)
        app.get("dust-caps/{id}", ::dustCap)
        app.put("dust-caps/{id}/connect", ::connect)
        app.put("dust-caps/{id}/disconnect", ::disconnect)
        app.put("dust-caps/{id}/park", ::park)
        app.put("dust-caps/{id}/unpark", ::unpark)
        app.put("dust-caps/{id}/listen", ::listen)
    }

    fun dustCaps(ctx: Context) {
        ctx.json(connectionService.dustCaps().sorted())
    }

    fun dustCap(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.dustCap(id)?.also(ctx::json)
    }

    fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.connect(dustCap)
    }

    fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.disconnect(dustCap)
    }

    fun park(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.park(dustCap)
    }

    fun unpark(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.unpark(dustCap)
    }

    fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.listen(dustCap)
    }
}
