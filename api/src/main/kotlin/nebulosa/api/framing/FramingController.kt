package nebulosa.api.framing

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.core.Controller
import nebulosa.api.image.ImageService
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.range
import nebulosa.math.deg
import nebulosa.math.hours

class FramingController(
    override val app: Javalin,
    private val imageService: ImageService,
    private val framingService: FramingService,
) : Controller {

    init {
        app.get("framing/hips-surveys", ::hipsSurveys)
        app.put("framing", ::frame)
    }

    private fun hipsSurveys(ctx: Context) {
        ctx.json(framingService.availableHipsSurveys)
    }

    private fun frame(ctx: Context) {
        val rightAscension = ctx.queryParam("rightAscension").notNullOrBlank()
        val declination = ctx.queryParam("declination").notNullOrBlank()
        val width = ctx.queryParam("width")?.toInt()?.range(1, 7680) ?: 1280
        val height = ctx.queryParam("height")?.toInt()?.range(1, 4320) ?: 720
        val fov = ctx.queryParam("fov")?.toDouble()?.range(0.0, 90.0) ?: 1.0
        val rotation = ctx.queryParam("rotation")?.toDouble() ?: 0.0
        val hipsSurvey = ctx.queryParam("hipsSurvey") ?: "CDS/P/DSS2/COLOR"

        imageService
            .frame(rightAscension.hours, declination.deg, width, height, fov.deg, rotation.deg, hipsSurvey)
            .also(ctx::json)
    }
}
