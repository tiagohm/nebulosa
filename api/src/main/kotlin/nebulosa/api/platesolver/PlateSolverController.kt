package nebulosa.api.platesolver

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class PlateSolverController(
    override val app: Application,
    private val plateSolverService: PlateSolverService,
) : Controller {

    init {
        with(app) {
            routing {
                put("/plate-solver/start", ::start)
                put("/plate-solver/stop", ::stop)
            }
        }
    }

    private suspend fun start(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters["path"].notNullOrBlank().path().exists()
        val key = queryParameters["key"].notNullOrBlank()
        val solver = receive<PlateSolverRequest>().valid()
        respond(plateSolverService.start(solver, path, key))
    }

    private fun stop(ctx: RoutingContext) = with(ctx.call) {
        plateSolverService.stop(queryParameters["key"].notNullOrBlank())
    }
}
