package nebulosa.api.livestacker

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.queryParamAsPath
import nebulosa.api.javalin.validate

class LiveStackingController(
    app: Javalin,
    private val liveStackingService: LiveStackingService,
    private val connectionService: ConnectionService,
) {

    init {
        app.put("live-stacking/{camera}/start", ::start)
        app.put("live-stacking/{camera}/add", ::add)
        app.put("live-stacking/{camera}/stop", ::stop)
    }

    private fun start(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        val body = ctx.bodyValidator<LiveStackingRequest>().validate().get()
        liveStackingService.start(camera, body)
    }

    private fun add(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        val path = ctx.queryParamAsPath("path").exists().get()
        liveStackingService.add(camera, path)?.also(ctx::json)
    }

    private fun stop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera"))!!
        liveStackingService.stop(camera)
    }
}
