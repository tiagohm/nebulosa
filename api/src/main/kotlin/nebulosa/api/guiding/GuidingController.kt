package nebulosa.api.guiding

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.valid
import kotlin.math.min

class GuidingController(
    app: Javalin,
    private val guidingService: GuidingService,
) {

    init {
        app.put("guiding/connect", ::connect)
        app.put("guiding/disconnect", ::disconnect)
        app.get("guiding/history", ::history)
        app.get("guiding/history/latest", ::latestHistory)
        app.put("guiding/history/clear", ::clearHistory)
        app.put("guiding/loop", ::loop)
        app.put("guiding/start", ::start)
        app.get("guiding/status", ::status)
        app.put("guiding/settle", ::settle)
        app.put("guiding/dither", ::dither)
        app.put("guiding/stop", ::stop)
    }

    private fun connect(ctx: Context) {
        val host = ctx.queryParam("host")?.ifBlank { null } ?: "localhost"
        val port = ctx.queryParam("port")?.toInt() ?: 4400
        guidingService.connect(host, port)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun disconnect(ctx: Context) {
        guidingService.disconnect()
    }

    private fun status(ctx: Context) {
        ctx.json(guidingService.status())
    }

    private fun history(ctx: Context) {
        val maxLength = min(100, ctx.queryParam("maxLength")?.toInt() ?: 100)
        ctx.json(guidingService.history(maxLength))
    }

    private fun latestHistory(ctx: Context) {
        guidingService.latestHistory()?.also(ctx::json)
    }


    @Suppress("UNUSED_PARAMETER")
    private fun clearHistory(ctx: Context) {
        return guidingService.clearHistory()
    }

    private fun loop(ctx: Context) {
        val autoSelectGuideStar = ctx.queryParam("autoSelectGuideStar")?.toBoolean() ?: true
        guidingService.loop(autoSelectGuideStar)
    }

    private fun start(ctx: Context) {
        val forceCalibration = ctx.queryParam("forceCalibration")?.toBoolean() ?: false
        guidingService.start(forceCalibration)
    }

    private fun settle(ctx: Context) {
        val body = ctx.bodyAsClass<SettleInfo>().valid()
        guidingService.settle(body)
    }

    private fun dither(ctx: Context) {
        val amount = ctx.queryParam("amount").notNull().toDouble()
        val raOnly = ctx.queryParam("raOnly")?.toBoolean() ?: false
        return guidingService.dither(amount, raOnly)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stop(ctx: Context) {
        guidingService.stop()
    }
}
