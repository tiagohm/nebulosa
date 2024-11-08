package nebulosa.api.alignment.polar

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.alignment.polar.darv.DARVStartRequest
import nebulosa.api.alignment.polar.tppa.TPPAStartRequest
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid

class PolarAlignmentController(
    override val app: Application,
    private val polarAlignmentService: PolarAlignmentService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(app) {
            routing {
                put("/polar-alignment/darv/{camera}/{guideOutput}/start", ::darvStart)
                put("/polar-alignment/darv/{camera}/stop", ::darvStop)
                get("/polar-alignment/darv/{camera}/status", ::darvStatus)
                put("/polar-alignment/tppa/{camera}/{mount}/start", ::tppaStart)
                put("/polar-alignment/tppa/{camera}/stop", ::tppaStop)
                put("/polar-alignment/tppa/{camera}/pause", ::tppaPause)
                put("/polar-alignment/tppa/{camera}/unpause", ::tppaUnpause)
                get("/polar-alignment/tppa/{camera}/status", ::tppaStatus)
            }
        }
    }

    private suspend fun darvStart(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val guideOutput = connectionService.guideOutput(pathParameters[GUIDE_OUTPUT].notNull()).notNull()
        val body = receive<DARVStartRequest>().valid()
        polarAlignmentService.darvStart(camera, guideOutput, body)
    }

    private fun darvStop(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        polarAlignmentService.darvStop(camera)
    }

    private suspend fun darvStatus(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        respondNullable(polarAlignmentService.darvStatus(camera))
    }

    private suspend fun tppaStart(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        val mount = connectionService.mount(pathParameters[MOUNT].notNull()).notNull()
        val body = receive<TPPAStartRequest>().valid()
        polarAlignmentService.tppaStart(camera, mount, body)
    }

    private fun tppaStop(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        polarAlignmentService.tppaStop(camera)
    }

    private fun tppaPause(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        polarAlignmentService.tppaPause(camera)
    }

    private fun tppaUnpause(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()) ?: return
        polarAlignmentService.tppaUnpause(camera)
    }

    private suspend fun tppaStatus(ctx: RoutingContext) = with(ctx.call) {
        val camera = connectionService.camera(pathParameters[CAMERA].notNull()).notNull()
        respondNullable(polarAlignmentService.tppaStatus(camera))
    }

    companion object {

        private const val CAMERA = "camera"
        private const val MOUNT = "mount"
        private const val GUIDE_OUTPUT = "guideOutput"
    }
}
