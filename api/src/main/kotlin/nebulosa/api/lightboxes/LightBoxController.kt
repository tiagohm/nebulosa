package nebulosa.api.lightboxes

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.positiveOrZero

class LightBoxController(
    override val server: Application,
    private val connectionService: ConnectionService,
    private val lightBoxService: LightBoxService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/light-boxes", ::lightBoxes)
                get("/light-boxes/{id}", ::lightBox)
                put("/light-boxes/{id}/connect", ::connect)
                put("/light-boxes/{id}/disconnect", ::disconnect)
                put("/light-boxes/{id}/enable", ::enable)
                put("/light-boxes/{id}/disable", ::disable)
                put("/light-boxes/{id}/brightness", ::brightness)
                put("/light-boxes/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun lightBoxes(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.lightBoxes().sorted())
    }

    private suspend fun lightBox(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.lightBox(id))
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.connect(lightBox)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.disconnect(lightBox)
    }

    private fun enable(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.enable(lightBox)
    }

    private fun disable(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.disable(lightBox)
    }

    private fun brightness(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val lightBox = connectionService.lightBox(id) ?: return
        val intensity = queryParameters["intensity"].notNull().toDouble().positiveOrZero()
        lightBoxService.brightness(lightBox, intensity)
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val lightBox = connectionService.lightBox(id) ?: return
        lightBoxService.listen(lightBox)
    }

    companion object {

        private const val ID = "id"
    }
}
