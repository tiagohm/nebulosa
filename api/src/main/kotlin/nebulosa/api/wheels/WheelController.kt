package nebulosa.api.wheels

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.positiveOrZero

class WheelController(
    override val app: Application,
    private val connectionService: ConnectionService,
    private val wheelService: WheelService,
) : Controller {

    init {
        with(app) {
            routing {
                get("/wheels", ::wheels)
                get("/wheels/{id}", ::wheel)
                put("/wheels/{id}/connect", ::connect)
                put("/wheels/{id}/disconnect", ::disconnect)
                put("/wheels/{id}/move-to", ::moveTo)
                put("/wheels/{id}/sync", ::sync)
                put("/wheels/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun wheels(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.wheels().sorted())
    }

    private suspend fun wheel(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.wheel(id))
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val wheel = connectionService.wheel(id) ?: return
        wheelService.connect(wheel)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val wheel = connectionService.wheel(id) ?: return
        wheelService.disconnect(wheel)
    }

    private fun moveTo(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val wheel = connectionService.wheel(id) ?: return
        val position = queryParameters[POSITION].notNull().toInt().positiveOrZero()
        wheelService.moveTo(wheel, position)
    }

    private fun sync(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val wheel = connectionService.wheel(id) ?: return
        val names = queryParameters[NAMES].notNullOrBlank()
        wheelService.sync(wheel, names.split(","))
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val wheel = connectionService.wheel(id) ?: return
        wheelService.listen(wheel)
    }

    companion object {

        private const val ID = "id"
        private const val POSITION = "position"
        private const val NAMES = "names"
    }
}
