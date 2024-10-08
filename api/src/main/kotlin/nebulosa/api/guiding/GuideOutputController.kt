package nebulosa.api.guiding

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.notBlank
import nebulosa.api.javalin.queryParamAsLong
import nebulosa.api.javalin.queryParamAsString
import nebulosa.api.javalin.range
import nebulosa.guiding.GuideDirection
import java.time.Duration

class GuideOutputController(
    app: Javalin,
    private val connectionService: ConnectionService,
    private val guideOutputService: GuideOutputService,
) {

    init {
        app.get("guide-outputs", ::guideOutputs)
        app.get("guide-outputs/{id}", ::guideOutput)
        app.put("guide-outputs/{id}/connect", ::connect)
        app.put("guide-outputs/{id}/disconnect", ::disconnect)
        app.put("guide-outputs/{id}/pulse", ::pulse)
        app.put("guide-outputs/{id}/listen", ::listen)
    }

    private fun guideOutputs(ctx: Context) {
        ctx.json(connectionService.guideOutputs().sorted())
    }

    private fun guideOutput(ctx: Context) {
        val id = ctx.pathParam("id")
        connectionService.guideOutput(id)?.also(ctx::json)
    }

    private fun connect(ctx: Context) {
        val id = ctx.pathParam("id")
        val guideOutput = connectionService.guideOutput(id) ?: return
        guideOutputService.connect(guideOutput)
    }

    private fun disconnect(ctx: Context) {
        val id = ctx.pathParam("id")
        val guideOutput = connectionService.guideOutput(id) ?: return
        guideOutputService.disconnect(guideOutput)
    }

    private fun pulse(ctx: Context) {
        val id = ctx.pathParam("id")
        val guideOutput = connectionService.guideOutput(id) ?: return
        val direction = ctx.queryParamAsString("direction").notBlank().get().let(GuideDirection::valueOf)
        val duration = ctx.queryParamAsLong("duration").range(PULSE_DURATION_RANGE).get().times(1000L).let(Duration::ofNanos)
        guideOutputService.pulse(guideOutput, direction, duration)
    }

    private fun listen(ctx: Context) {
        val id = ctx.pathParam("id")
        val guideOutput = connectionService.guideOutput(id) ?: return
        guideOutputService.listen(guideOutput)
    }

    companion object {

        private val PULSE_DURATION_RANGE = 0L..1800000000L
    }
}
