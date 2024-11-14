package nebulosa.api.rotators

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.range

class RotatorController(
    override val app: Application,
    private val connectionService: ConnectionService,
    private val rotatorService: RotatorService,
) : Controller {

    init {
        with(app) {
            routing {
                get("/rotators", ::rotators)
                get("/rotators/{id}", ::rotator)
                put("/rotators/{id}/connect", ::connect)
                put("/rotators/{id}/disconnect", ::disconnect)
                put("/rotators/{id}/reverse", ::reverse)
                put("/rotators/{id}/move", ::move)
                put("/rotators/{id}/abort", ::abort)
                put("/rotators/{id}/home", ::home)
                put("/rotators/{id}/sync", ::sync)
                put("/rotators/{id}/listen", ::listen)
            }
        }
    }

    private suspend fun rotators(ctx: RoutingContext) = with(ctx.call) {
        respond(connectionService.rotators().sorted())
    }

    private suspend fun rotator(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        respondNullable(connectionService.rotator(id))
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.connect(rotator)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.disconnect(rotator)
    }

    private fun reverse(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        val enabled = queryParameters["enabled"].notNull().toBoolean()
        rotatorService.reverse(rotator, enabled)
    }

    private fun move(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        val angle = queryParameters["angle"].notNull().toDouble().range(0.0, 360.0)
        rotatorService.move(rotator, angle)
    }

    private fun abort(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.abort(rotator)
    }

    private fun home(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.home(rotator)
    }

    private fun sync(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        val angle = queryParameters["angle"].notNull().toDouble().range(0.0, 360.0)
        rotatorService.sync(rotator, angle)
    }

    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val id = pathParameters[ID].notNull()
        val rotator = connectionService.rotator(id) ?: return
        rotatorService.listen(rotator)
    }

    companion object {

        private const val ID = "id"
    }
}
