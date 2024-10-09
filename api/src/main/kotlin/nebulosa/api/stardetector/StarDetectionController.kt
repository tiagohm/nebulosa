package nebulosa.api.stardetector

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.notNull
import nebulosa.api.javalin.path
import nebulosa.api.javalin.valid

class StarDetectionController(
    app: Javalin,
    private val starDetectionService: StarDetectionService,
) {

    init {
        app.put("star-detection", ::detectStars)
    }

    private fun detectStars(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        val body = ctx.bodyAsClass<StarDetectionRequest>().valid()
        ctx.json(starDetectionService.detectStars(path, body))
    }
}
