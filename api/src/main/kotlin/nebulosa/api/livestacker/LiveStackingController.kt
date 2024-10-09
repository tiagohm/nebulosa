package nebulosa.api.livestacker

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.path
import nebulosa.api.javalin.valid

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
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        val body = ctx.bodyAsClass<LiveStackingRequest>().valid()
        liveStackingService.start(camera, body)
    }

    private fun add(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        val path = ctx.queryParam("path").notNull().path().exists()
        liveStackingService.add(camera, path)?.also(ctx::json)
    }

    private fun stop(ctx: Context) {
        val camera = connectionService.camera(ctx.pathParam("camera")).notNull()
        liveStackingService.stop(camera)
    }
}
