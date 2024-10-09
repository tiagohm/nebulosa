package nebulosa.api.rotators

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.range

class RotatorController(
    app: Javalin,
    private val connectionService: ConnectionService,
    private val rotatorService: RotatorService,
) {

    init {
        app.get("rotators", ::rotators)
        app.get("rotators/{id}", ::rotator)
        app.put("rotators/{id}/connect", ::connect)
        app.put("rotators/{id}/disconnect", ::disconnect)
        app.put("rotators/{id}/reverse", ::reverse)
        app.put("rotators/{id}/move", ::move)
        app.put("rotators/{id}/abort", ::abort)
        app.put("rotators/{id}/home", ::home)
        app.put("rotators/{id}/sync", ::sync)
        app.put("rotators/{id}/listen", ::listen)
    }

    private fun rotators(ctx: Context) {
        ctx.json(connectionService.rotators().sorted())
    }

    private fun rotator(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.rotator(id)?.also(ctx::json)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.connect(rotator)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.disconnect(rotator)
    }

    private fun reverse(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        val enabled = ctx.queryParam("enabled").notNull().toBoolean()
        rotatorService.reverse(rotator, enabled)
    }

    private fun move(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        val angle = ctx.queryParam("angle").notNull().toDouble().range(0.0, 360.0)
        rotatorService.move(rotator, angle)
    }

    private fun abort(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.abort(rotator)
    }

    private fun home(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.home(rotator)
    }

    private fun sync(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        val angle = ctx.queryParam("angle").notNull().toDouble().range(0.0, 360.0)
        rotatorService.sync(rotator, angle)
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.listen(rotator)
    }
}
