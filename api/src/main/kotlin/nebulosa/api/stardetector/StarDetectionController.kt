package nebulosa.api.stardetector

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.api.validators.valid

class StarDetectionController(
    override val app: Application,
    private val starDetectionService: StarDetectionService,
) : Controller {

    init {
        with(app) {
            routing {
                put("/star-detection", ::detectStars)
            }
        }
    }

    private suspend fun detectStars(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters["path"].notNull().path().exists()
        val body = receive<StarDetectionRequest>().valid()
        respond(starDetectionService.detectStars(path, body))
    }
}
