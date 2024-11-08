package nebulosa.api.wizard.flat

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid

class FlatWizardController(
    override val app: Application,
    private val flatWizardService: FlatWizardService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(app) {
            routing {
                put("/flat-wizard/{camera}/start", ::start)
                put("/flat-wizard/{camera}/stop", ::stop)
                get("/flat-wizard/{camera}/status", ::status)
            }
        }
    }

    private suspend fun start(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val body = receive<FlatWizardRequest>().valid()
        flatWizardService.start(camera, body)
    }

    private fun stop(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        flatWizardService.stop(camera)
    }

    private suspend fun status(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        respondNullable(flatWizardService.status(camera))
    }

    companion object {

        private const val CAMERA = "camera"
    }
}
