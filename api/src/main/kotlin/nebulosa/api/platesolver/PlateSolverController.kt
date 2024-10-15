package nebulosa.api.platesolver

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.core.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class PlateSolverController(
    override val app: Javalin,
    private val plateSolverService: PlateSolverService,
) : Controller {

    init {
        app.put("plate-solver/start", ::start)
        app.put("plate-solver/stop", ::stop)
    }

    private fun start(ctx: Context) {
        val path = ctx.queryParam("path").notNullOrBlank().path().exists()
        val key = ctx.queryParam("key").notNullOrBlank()
        val solver = ctx.bodyAsClass<PlateSolverRequest>().valid()
        ctx.json(plateSolverService.start(solver, path, key))
    }

    private fun stop(ctx: Context) {
        plateSolverService.stop(ctx.queryParam("key").notNullOrBlank())
    }
}
