package nebulosa.api.indi

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid

class INDIController(
    override val app: Application,
    private val indiService: INDIService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        with(app) {
            routing {
                get("/indi/{device}", ::device)
                put("/indi/{device}/connect", ::connect)
                put("/indi/{device}/disconnect", ::disconnect)
                get("/indi/{device}/properties", ::properties)
                put("/indi/{device}/send", ::sendProperty)
                get("/indi/{device}/log", ::deviceLog)
                put("/indi/{device}/listen", ::listen)
                put("/indi/{device}/unlisten", ::unlisten)
                get("/indi/log", ::log)
            }
        }
    }

    private suspend fun device(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()).notNull()
        respond(device)
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()) ?: return
        indiService.connect(device)
    }

    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()) ?: return
        indiService.disconnect(device)
    }

    private suspend fun properties(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()).notNull()
        respond(indiService.properties(device))
    }

    private suspend fun sendProperty(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()) ?: return
        val body = receive<INDISendProperty>().valid()
        indiService.sendProperty(device, body)
    }

    private suspend fun deviceLog(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()).notNull()
        respond(synchronized(device.messages) { device.messages })
    }

    private suspend fun log(ctx: RoutingContext) = with(ctx.call) {
        respond(indiService.messages())
    }

    @Synchronized
    private fun listen(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()) ?: return
        indiService.registerDeviceToSendMessage(device)
    }

    @Synchronized
    private fun unlisten(ctx: RoutingContext) = with(ctx.call) {
        val device = connectionService.device(pathParameters[DEVICE].notNull()) ?: return
        indiService.unregisterDeviceToSendMessage(device)
    }

    companion object {

        private const val DEVICE = "device"
    }
}
