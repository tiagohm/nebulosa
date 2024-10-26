package nebulosa.api.wheels

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.http.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.positiveOrZero

class WheelController(
    override val app: Javalin,
    private val connectionService: ConnectionService,
    private val wheelService: WheelService,
) : Controller {

    init {
        app.get("wheels", ::wheels)
        app.get("wheels/{id}", ::wheel)
        app.put("wheels/{id}/connect", ::connect)
        app.put("wheels/{id}/disconnect", ::disconnect)
        app.put("wheels/{id}/move-to", ::moveTo)
        app.put("wheels/{id}/sync", ::sync)
        app.put("wheels/{id}/listen", ::listen)
    }

    private fun wheels(ctx: Context) {
        ctx.json(connectionService.wheels().sorted())
    }

    private fun wheel(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.wheel(id)?.also(ctx::json)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val wheel = connectionService.wheel(id) ?: return
        wheelService.connect(wheel)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val wheel = connectionService.wheel(id) ?: return
        wheelService.disconnect(wheel)
    }

    private fun moveTo(ctx: Context) {
        val id = ctx.pathParam("id")
        val wheel = connectionService.wheel(id) ?: return
        val position = ctx.queryParam("position").notNull().toInt().positiveOrZero()
        wheelService.moveTo(wheel, position)
    }

    private fun sync(ctx: Context) {
        val id = ctx.pathParam("id")
        val wheel = connectionService.wheel(id) ?: return
        val names = ctx.queryParam("names").notNullOrBlank()
        wheelService.sync(wheel, names.split(","))
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val wheel = connectionService.wheel(id) ?: return
        wheelService.listen(wheel)
    }
}
