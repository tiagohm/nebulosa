package nebulosa.api.autofocus

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.core.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid

class AutoFocusController(
    override val app: Javalin,
    private val autoFocusService: AutoFocusService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        app.put("auto-focus/{camera}/{focuser}/start", ::start)
        app.put("auto-focus/{camera}/stop", ::stop)
        app.get("auto-focus/{camera}/status", ::status)
    }

    private fun start(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        val focuser = connectionService.focuser(ctx.pathParam("focuser")).notNull()
        val body = ctx.bodyAsClass<AutoFocusRequest>().valid()
        autoFocusService.start(camera, focuser, body)
    }

    private fun stop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        autoFocusService.stop(camera)
    }

    private fun status(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        autoFocusService.status(camera)?.also(ctx::json)
    }
}
