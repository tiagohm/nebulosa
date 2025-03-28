package nebulosa.api.guiding

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNull
import nebulosa.api.validators.valid
import kotlin.math.min

class GuidingController(
    override val app: Application,
    private val guidingService: GuidingService,
) : Controller {

    init {
        with(app) {
            routing {
                put("/guiding/connect", ::connect)
                put("/guiding/disconnect", ::disconnect)
                get("/guiding/history", ::history)
                get("/guiding/history/latest", ::latestHistory)
                put("/guiding/history/clear", ::clearHistory)
                put("/guiding/loop", ::loop)
                put("/guiding/start", ::start)
                get("/guiding/status", ::status)
                put("/guiding/settle", ::settle)
                put("/guiding/dither", ::dither)
                put("/guiding/stop", ::stop)
            }
        }
    }

    private fun connect(ctx: RoutingContext) = with(ctx.call) {
        val host = queryParameters["host"]?.ifBlank { null } ?: "localhost"
        val port = queryParameters["port"]?.toInt() ?: 4400
        guidingService.connect(host, port)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun disconnect(ctx: RoutingContext) = with(ctx.call) {
        guidingService.disconnect()
    }

    private suspend fun status(ctx: RoutingContext) = with(ctx.call) {
        respond(guidingService.status())
    }

    private suspend fun history(ctx: RoutingContext) = with(ctx.call) {
        val maxLength = min(100, queryParameters["maxLength"]?.toInt() ?: 100)
        respond(guidingService.history(maxLength))
    }

    private suspend fun latestHistory(ctx: RoutingContext) = with(ctx.call) {
        respondNullable(guidingService.latestHistory())
    }


    @Suppress("UNUSED_PARAMETER")
    private fun clearHistory(ctx: RoutingContext) = with(ctx.call) {
        guidingService.clearHistory()
    }

    private fun loop(ctx: RoutingContext) = with(ctx.call) {
        val autoSelectGuideStar = queryParameters["autoSelectGuideStar"]?.toBoolean() != false
        guidingService.loop(autoSelectGuideStar)
    }

    private fun start(ctx: RoutingContext) = with(ctx.call) {
        val forceCalibration = queryParameters["forceCalibration"]?.toBoolean() == true
        guidingService.start(forceCalibration)
    }

    private suspend fun settle(ctx: RoutingContext) = with(ctx.call) {
        val body = receive<SettleInfo>().valid()
        guidingService.settle(body)
    }

    private fun dither(ctx: RoutingContext) = with(ctx.call) {
        val amount = queryParameters["amount"].notNull().toDouble()
        val raOnly = queryParameters["raOnly"]?.toBoolean() == true
        guidingService.dither(amount, raOnly)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stop(ctx: RoutingContext) = with(ctx.call) {
        guidingService.stop()
    }
}
