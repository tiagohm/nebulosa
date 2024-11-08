package nebulosa.api.framing

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import nebulosa.api.image.ImageService
import nebulosa.api.ktor.Controller
import nebulosa.api.validators.notNullOrBlank
import nebulosa.api.validators.range
import nebulosa.math.deg
import nebulosa.math.hours

class FramingController(
    override val server: Application,
    private val imageService: ImageService,
    private val framingService: FramingService,
) : Controller {

    init {
        with(server) {
            routing {
                get("/framing/hips-surveys", ::hipsSurveys)
                put("/framing", ::frame)
            }
        }
    }

    private suspend fun hipsSurveys(ctx: RoutingContext) = with(ctx.call) {
        respond(framingService.availableHipsSurveys)
    }

    private suspend fun frame(ctx: RoutingContext) = with(ctx.call) {
        val rightAscension = queryParameters["rightAscension"].notNullOrBlank()
        val declination = queryParameters["declination"].notNullOrBlank()
        val width = queryParameters["width"]?.toInt()?.range(1, 7680) ?: 1280
        val height = queryParameters["height"]?.toInt()?.range(1, 4320) ?: 720
        val fov = queryParameters["fov"]?.toDouble()?.range(0.0, 90.0) ?: 1.0
        val rotation = queryParameters["rotation"]?.toDouble() ?: 0.0
        val hipsSurvey = queryParameters["hipsSurvey"] ?: "CDS/P/DSS2/COLOR"

        val path = imageService
            .frame(rightAscension.hours, declination.deg, width, height, fov.deg, rotation.deg, hipsSurvey)
        respond(path)
    }
}
