package nebulosa.api.platesolver

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.path
import nebulosa.api.javalin.valid

class PlateSolverController(
    app: Javalin,
    private val plateSolverService: PlateSolverService,
) {

    init {
        app.put("plate-solver/start", ::start)
        app.put("plate-solver/stop", ::stop)
    }

    private fun start(ctx: Context) {
        val path = ctx.queryParam("path")?.path().notNull().exists()
        val solver = ctx.bodyAsClass<PlateSolverRequest>().valid()
        ctx.json(plateSolverService.solveImage(solver, path))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stop(ctx: Context) {
        plateSolverService.stopSolver()
    }
}
