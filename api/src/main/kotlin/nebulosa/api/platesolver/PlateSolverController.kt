package nebulosa.api.platesolver

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.queryParamAsPath
import nebulosa.api.javalin.validate

class PlateSolverController(
    app: Javalin,
    private val plateSolverService: PlateSolverService,
) {

    init {
        app.put("plate-solver/start", ::start)
        app.put("plate-solver/stop", ::stop)
    }

    private fun start(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        val solver = ctx.bodyValidator<PlateSolverRequest>().validate().get()
        plateSolverService.solveImage(solver, path)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stop(ctx: Context) {
        plateSolverService.stopSolver()
    }
}
