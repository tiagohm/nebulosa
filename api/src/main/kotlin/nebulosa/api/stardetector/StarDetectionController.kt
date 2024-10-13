package nebulosa.api.stardetector

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.core.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class StarDetectionController(
    override val app: Javalin,
    private val starDetectionService: StarDetectionService,
) : Controller {

    init {
        app.put("star-detection", ::detectStars)
    }

    private fun detectStars(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        val body = ctx.bodyAsClass<StarDetectionRequest>().valid()
        ctx.json(starDetectionService.detectStars(path, body))
    }
}
