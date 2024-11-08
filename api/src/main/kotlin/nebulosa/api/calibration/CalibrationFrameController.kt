package nebulosa.api.calibration

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class CalibrationFrameController(
    override val server: Application,
    private val calibrationFrameService: CalibrationFrameService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/calibration-frames", ::groups)
                get("/calibration-frames/{group}", ::frames)
                put("/calibration-frames/{group}", ::upload)
                post("/calibration-frames", ::update)
                delete("/calibration-frames/{id}", ::delete)
            }
        }
    }

    private suspend fun groups(ctx: RoutingContext) = with(ctx.call) {
        respond(calibrationFrameService.groups())
    }

    private suspend fun frames(ctx: RoutingContext) = with(ctx.call) {
        val group = pathParameters[GROUP].notNull()
        respond(calibrationFrameService.frames(group).sorted())
    }

    private suspend fun upload(ctx: RoutingContext) = with(ctx.call) {
        val group = pathParameters[GROUP].notNull()
        val path = queryParameters[PATH].notNull().path().exists()
        respond(calibrationFrameService.upload(group, path))
    }

    private suspend fun update(ctx: RoutingContext) = with(ctx.call) {
        val body = receive<CalibrationFrameEntity>().valid()
        respond(calibrationFrameService.edit(body))
    }

    private fun delete(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull().toLong()
        calibrationFrameService.delete(id)
    }

    companion object {

        private const val ID = "id"
        private const val PATH = "path"
        private const val GROUP = "group"
    }
}
