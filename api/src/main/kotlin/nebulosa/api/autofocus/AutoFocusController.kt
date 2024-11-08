package nebulosa.api.autofocus

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

class AutoFocusController(
    override val server: Application,
    private val autoFocusService: AutoFocusService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(server) {
            routing {
                put("/auto-focus/{camera}/{focuser}/start", ::start)
                put("/auto-focus/{camera}/stop", ::stop)
                get("/auto-focus/{camera}/status", ::status)
            }
        }
    }

    private suspend fun start(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val focuser = connectionService.focuser(pathParameters[FOCUSER].notNull()).notNull()
        val body = receive<AutoFocusRequest>().valid()
        autoFocusService.start(camera, focuser, body)
    }

    private fun stop(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        autoFocusService.stop(camera)
    }

    private suspend fun status(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        respondNullable(autoFocusService.status(camera))
    }

    companion object {

        private const val CAMERA = "camera"
        private const val FOCUSER = "focuser"
    }
}
