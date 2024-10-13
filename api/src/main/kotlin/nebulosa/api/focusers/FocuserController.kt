package nebulosa.api.focusers

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.core.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.positiveOrZero

class FocuserController(
    override val app: Javalin,
    private val connectionService: ConnectionService,
    private val focuserService: FocuserService,
) : Controller {

    init {
        app.get("focusers", ::focusers)
        app.get("focusers/{id}", ::focuser)
        app.put("focusers/{id}/connect", ::connect)
        app.put("focusers/{id}/disconnect", ::disconnect)
        app.put("focusers/{id}/move-in", ::moveIn)
        app.put("focusers/{id}/move-out", ::moveOut)
        app.put("focusers/{id}/move-to", ::moveTo)
        app.put("focusers/{id}/abort", ::abort)
        app.put("focusers/{id}/sync", ::sync)
        app.put("focusers/{id}/listen", ::listen)
    }

    private fun focusers(ctx: Context) {
        ctx.json(connectionService.focusers().sorted())
    }

    private fun focuser(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        ctx.json(focuser)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        focuserService.connect(focuser)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        focuserService.disconnect(focuser)
    }

    private fun moveIn(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        val steps = ctx.queryParam("steps").notNull().toInt().positiveOrZero()
        focuserService.moveIn(focuser, steps)
    }

    private fun moveOut(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        val steps = ctx.queryParam("steps").notNull().toInt().positiveOrZero()
        focuserService.moveOut(focuser, steps)
    }

    private fun moveTo(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        val steps = ctx.queryParam("steps").notNull().toInt().positiveOrZero()
        focuserService.moveTo(focuser, steps)
    }

    private fun abort(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        focuserService.abort(focuser)
    }

    private fun sync(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        val steps = ctx.queryParam("steps").notNull().toInt().positiveOrZero()
        focuserService.sync(focuser, steps)
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val focuser = connectionService.focuser(id) ?: return
        focuserService.listen(focuser)
    }
}
