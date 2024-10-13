package nebulosa.api.dustcap

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.core.Controller

class DustCapController(
    override val app: Javalin,
    private val connectionService: ConnectionService,
    private val dustCapService: DustCapService,
) : Controller {

    init {
        app.get("dust-caps", ::dustCaps)
        app.get("dust-caps/{id}", ::dustCap)
        app.put("dust-caps/{id}/connect", ::connect)
        app.put("dust-caps/{id}/disconnect", ::disconnect)
        app.put("dust-caps/{id}/park", ::park)
        app.put("dust-caps/{id}/unpark", ::unpark)
        app.put("dust-caps/{id}/listen", ::listen)
    }

    private fun dustCaps(ctx: Context) {
        ctx.json(connectionService.dustCaps().sorted())
    }

    private fun dustCap(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.dustCap(id)?.also(ctx::json)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.connect(dustCap)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.disconnect(dustCap)
    }

    private fun park(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.park(dustCap)
    }

    private fun unpark(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.unpark(dustCap)
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val dustCap = connectionService.dustCap(id) ?: return
        dustCapService.listen(dustCap)
    }
}
