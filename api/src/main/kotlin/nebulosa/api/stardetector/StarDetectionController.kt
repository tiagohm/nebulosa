package nebulosa.api.stardetector

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.queryParamAsPath
import nebulosa.api.javalin.validate

class StarDetectionController(
    app: Javalin,
    private val starDetectionService: StarDetectionService,
) {

    init {
        app.put("star-detection", ::detectStars)
    }

    fun detectStars(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        val body = ctx.bodyValidator<StarDetectionRequest>().validate().get()
        ctx.json(starDetectionService.detectStars(path, body))
    }
}
