package nebulosa.api.livestacker

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class LiveStackingController(
    override val app: Application,
    private val liveStackingService: LiveStackingService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(app) {
            routing {
                put("/live-stacking/{camera}/start", ::start)
                put("/live-stacking/{camera}/add", ::add)
                put("/live-stacking/{camera}/stop", ::stop)
            }
        }
    }

    private suspend fun start(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val body = receive<LiveStackingRequest>().valid()
        liveStackingService.start(camera, body)
    }

    private suspend fun add(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val path = queryParameters["path"].notNull().path().exists()
        respondNullable(liveStackingService.add(camera, path))
    }

    private fun stop(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        liveStackingService.stop(camera)
    }

    companion object {

        private const val CAMERA = "camera"
    }
}
