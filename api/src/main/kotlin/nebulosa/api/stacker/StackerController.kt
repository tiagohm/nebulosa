package nebulosa.api.stacker

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.core.Controller
import nebulosa.api.validators.*

class StackerController(
    override val app: Javalin,
    private val stackerService: StackerService,
) : Controller {

    init {
        app.put("stacker/start", ::start)
        app.get("stacker/running", ::isRunning)
        app.put("stacker/stop", ::stop)
        app.put("stacker/analyze", ::analyze)
    }

    private fun start(ctx: Context) {
        val key = ctx.queryParam("key").notNullOrBlank()
        val body = ctx.bodyAsClass<StackingRequest>().valid()
        stackerService.stack(body, key)?.also(ctx::json)
    }

    private fun isRunning(ctx: Context) {
        val key = ctx.queryParam("key").notNullOrBlank()
        ctx.json(stackerService.isRunning(key))
    }

    private fun stop(ctx: Context) {
        val key = ctx.queryParam("key").notNullOrBlank()
        stackerService.stop(key)
    }

    private fun analyze(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        stackerService.analyze(path)?.also(ctx::json)
    }
}
