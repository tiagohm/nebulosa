package nebulosa.api.indi

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.http.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid

class INDIController(
    override val app: Javalin,
    private val indiService: INDIService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        app.get("indi/{device}", ::device)
        app.put("indi/{device}/connect", ::connect)
        app.put("indi/{device}/disconnect", ::disconnect)
        app.get("indi/{device}/properties", ::properties)
        app.put("indi/{device}/send", ::sendProperty)
        app.get("indi/{device}/log", ::deviceLog)
        app.put("indi/{device}/listen", ::listen)
        app.put("indi/{device}/unlisten", ::unlisten)
        app.get("indi/log", ::log)
    }

    private fun device(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")).notNull()
        ctx.json(device)
    }

    private fun connect(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")) ?: return
        indiService.connect(device)
    }

    private fun disconnect(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")) ?: return
        indiService.disconnect(device)
    }

    private fun properties(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")).notNull()
        ctx.json(indiService.properties(device))
    }

    private fun sendProperty(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")) ?: return
        val body = ctx.bodyAsClass<INDISendProperty>().valid()
        indiService.sendProperty(device, body)
    }

    private fun deviceLog(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")).notNull()
        ctx.json(synchronized(device.messages) { device.messages })
    }

    private fun log(ctx: Context) {
        ctx.json(indiService.messages())
    }

    @Synchronized
    private fun listen(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")) ?: return
        indiService.registerDeviceToSendMessage(device)
    }

    @Synchronized
    private fun unlisten(ctx: Context) {
        val device = connectionService.device(ctx.pathParam("device")) ?: return
        indiService.unregisterDeviceToSendMessage(device)
    }
}
