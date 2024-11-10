package nebulosa.api.cameras

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.range
import nebulosa.api.validators.valid

class CameraController(
    override val app: Application,
    private val connectionService: ConnectionService,
    private val cameraService: CameraService,
) : Controller {

    init {
        with(app) {
            routing {
                get("/cameras", ::cameras)
                get("/cameras/{id}", ::camera)
                put("/cameras/{id}/connect", ::connect)
                put("/cameras/{id}/disconnect", ::disconnect)
                put("/cameras/{id}/snoop", ::snoop)
                put("/cameras/{id}/cooler", ::cooler)
                put("/cameras/{id}/temperature/setpoint", ::setpointTemperature)
                put("/cameras/{id}/capture/start", ::startCapture)
                put("/cameras/{id}/capture/pause", ::pauseCapture)
                put("/cameras/{id}/capture/unpause", ::unpauseCapture)
                put("/cameras/{id}/capture/abort", ::abortCapture)
                get("/cameras/{id}/capture/status", ::captureStatus)
                put("/cameras/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun cameras(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.cameras().sorted())
    }

    private suspend fun camera(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id).notNull()
        respond(camera)
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        cameraService.connect(camera)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        cameraService.disconnect(camera)
    }

    private fun snoop(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        val mount = queryParameters[MOUNT]?.let(connectionService::mount)
        val wheel = queryParameters[WHEEL]?.let(connectionService::wheel)
        val focuser = queryParameters[FOCUSER]?.let(connectionService::focuser)
        val rotator = queryParameters[ROTATOR]?.let(connectionService::rotator)
        cameraService.snoop(camera, mount, wheel, focuser, rotator)
    }

    private fun cooler(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        val enabled = queryParameters[ENABLED].notNull().toBoolean()
        cameraService.cooler(camera, enabled)
    }

    private fun setpointTemperature(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        val temperature = queryParameters[TEMPERATURE].notNull().toDouble().range(-50.0, 50.0)
        cameraService.setpointTemperature(camera, temperature)
    }

    private suspend fun startCapture(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        val mount = queryParameters[MOUNT]?.let(connectionService::mount)
        val wheel = queryParameters[WHEEL]?.let(connectionService::wheel)
        val focuser = queryParameters[FOCUSER]?.let(connectionService::focuser)
        val rotator = queryParameters[ROTATOR]?.let(connectionService::rotator)
        val body = receive<CameraStartCaptureRequest>().valid()
        cameraService.startCapture(camera, body, mount, wheel, focuser, rotator)
    }

    private fun pauseCapture(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        cameraService.pauseCapture(camera)
    }

    private fun unpauseCapture(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        cameraService.unpauseCapture(camera)
    }

    private fun abortCapture(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        cameraService.abortCapture(camera)
    }

    private suspend fun captureStatus(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        respondNullable(cameraService.captureStatus(camera))
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val camera = connectionService.camera(id) ?: return
        cameraService.listen(camera)
    }

    companion object {

        private const val ID = "id"
        private const val MOUNT = "mount"
        private const val WHEEL = "wheel"
        private const val FOCUSER = "focuser"
        private const val ROTATOR = "rotator"
        private const val ENABLED = "enabled"
        private const val TEMPERATURE = "temperature"
    }
}
