package nebulosa.api.sequencer

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.http.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid

class SequencerController(
    override val app: Javalin,
    private val sequencerService: SequencerService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        app.put("sequencer/{camera}/start", ::start)
        app.put("sequencer/{camera}/stop", ::stop)
        app.put("sequencer/{camera}/pause", ::pause)
        app.put("sequencer/{camera}/unpause", ::unpause)
        app.get("sequencer/{camera}/status", ::status)
    }

    private fun start(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        val mount = ctx.queryParam("mount")?.let(connectionService::mount)
        val wheel = ctx.queryParam("wheel")?.let(connectionService::wheel)
        val focuser = ctx.queryParam("focuser")?.let(connectionService::focuser)
        val rotator = ctx.queryParam("rotator")?.let(connectionService::rotator)
        val body = ctx.bodyAsClass<SequencerPlanRequest>().valid()
        sequencerService.start(camera, body, mount, wheel, focuser, rotator)
    }

    fun stop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        sequencerService.stop(camera)
    }

    fun pause(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        sequencerService.pause(camera)
    }

    fun unpause(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")) ?: return
        sequencerService.unpause(camera)
    }

    fun status(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        sequencerService.status(camera)?.also(ctx::json)
    }
}
