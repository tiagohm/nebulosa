package nebulosa.api.guiding

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.enumOf
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.range
import nebulosa.guiding.GuideDirection
import java.time.Duration

class GuideOutputController(
    override val server: Application,
    private val connectionService: ConnectionService,
    private val guideOutputService: GuideOutputService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/guide-outputs", ::guideOutputs)
                get("/guide-outputs/{id}", ::guideOutput)
                put("/guide-outputs/{id}/connect", ::connect)
                put("/guide-outputs/{id}/disconnect", ::disconnect)
                put("/guide-outputs/{id}/pulse", ::pulse)
                put("/guide-outputs/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun guideOutputs(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.guideOutputs().sorted())
    }

    private suspend fun guideOutput(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.guideOutput(id))
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val guideOutput = connectionService.guideOutput(id) ?: return
        guideOutputService.connect(guideOutput)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val guideOutput = connectionService.guideOutput(id) ?: return
        guideOutputService.disconnect(guideOutput)
    }

    private fun pulse(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val guideOutput = connectionService.guideOutput(id) ?: return
        val direction = queryParameters["direction"].notNullOrBlank().enumOf<GuideDirection>()
        val duration = queryParameters["duration"].notNull().toLong().range(0L, 1800000000L).times(1000L).let(Duration::ofNanos)
        guideOutputService.pulse(guideOutput, direction, duration)
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val guideOutput = connectionService.guideOutput(id) ?: return
        guideOutputService.listen(guideOutput)
    }

    companion object {

        private const val ID = "id"
    }
}
