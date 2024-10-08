package nebulosa.api.framing

import io.javalin.Javalin
import io.javalin.http.Context
import nebulosa.api.image.ImageService
import nebulosa.api.javalin.*
import nebulosa.math.deg
import nebulosa.math.hours

class FramingController(
    app: Javalin,
    private val imageService: ImageService,
    private val framingService: FramingService,
) {

    init {
        app.get("framing/hips-surveys", ::hipsSurveys)
        app.put("framing", ::frame)
    }

    private fun hipsSurveys(ctx: Context) {
        ctx.json(framingService.availableHipsSurveys)
    }

    private fun frame(ctx: Context) {
        val rightAscension = ctx.queryParamAsString("rightAscension").notBlank().get()
        val declination = ctx.queryParamAsString("declination").notBlank().get()
        val width = ctx.queryParamAsInt("width").range(1..7680).getOrDefault(1280)
        val height = ctx.queryParamAsInt("height").range(1..4320).getOrDefault(720)
        val fov = ctx.queryParamAsDouble("fov").range(0.0..90.0).getOrDefault(1.0)
        val rotation = ctx.queryParamAsDouble("fov").getOrDefault(0.0)
        val hipsSurvey = ctx.queryParamAsString("hipsSurvey").getOrDefault("CDS/P/DSS2/COLOR")

        imageService
            .frame(rightAscension.hours, declination.deg, width, height, fov.deg, rotation.deg, hipsSurvey)
            .also(ctx::json)
    }
}
