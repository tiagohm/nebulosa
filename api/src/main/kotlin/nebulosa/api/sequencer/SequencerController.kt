package nebulosa.api.sequencer

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

class SequencerController(
    override val server: Application,
    private val sequencerService: SequencerService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(server) {
            routing {
                put("/sequencer/{camera}/start", ::start)
                put("/sequencer/{camera}/stop", ::stop)
                put("/sequencer/{camera}/pause", ::pause)
                put("/sequencer/{camera}/unpause", ::unpause)
                get("/sequencer/{camera}/status", ::status)
            }
        }
    }

    private suspend fun start(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val mount = queryParameters[MOUNT]?.let(connectionService::mount)
        val wheel = queryParameters[WHEEL]?.let(connectionService::wheel)
        val focuser = queryParameters[FOCUSER]?.let(connectionService::focuser)
        val rotator = queryParameters[ROTATOR]?.let(connectionService::rotator)
        val body = receive<SequencerPlanRequest>().valid()
        sequencerService.start(camera, body, mount, wheel, focuser, rotator)
    }

    private fun stop(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        sequencerService.stop(camera)
    }

    private fun pause(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        sequencerService.pause(camera)
    }

    private fun unpause(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        sequencerService.unpause(camera)
    }

    private suspend fun status(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        respondNullable(sequencerService.status(camera))
    }

    companion object {

        private const val CAMERA = "camera"
        private const val MOUNT = "mount"
        private const val WHEEL = "wheel"
        private const val FOCUSER = "focuser"
        private const val ROTATOR = "rotator"
    }
}
