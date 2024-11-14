package nebulosa.api.image

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nebulosa.api.connection.ConnectionService
import nebulosa.api.ktor.Controller
import nebulosa.api.ktor.location
import nebulosa.api.validators.enumOf
import nebulosa.api.validators.exists
import nebulosa.api.validators.notEmpty
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.image.format.ImageChannel
import java.nio.file.Path
import java.util.*

class ImageController(
    override val app: Application,
    private val imageService: ImageService,
    private val connectionService: ConnectionService,
    private val mapper: ObjectMapper,
) : Controller {

    init {
        with(app) {
            routing {
                post("/image", ::openImage)
                post("/image/open-on-desktop", ::openImagesOnDesktop)
                delete("/image", ::closeImage)
                put("/image/save-as", ::saveImageAs)
                put("/image/analyze", ::analyze)
                put("/image/annotations", ::annotations)
                get("/image/coordinate-interpolation", ::coordinateInterpolation)
                post("/image/statistics", ::statistics)
                get("/image/fov-cameras", ::fovCameras)
                get("/image/fov-telescopes", ::fovTelescopes)
            }
        }
    }

    private suspend fun openImage(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path().exists()
        val camera = queryParameters[CAMERA]?.ifBlank { null }?.let(connectionService::camera)
        val transformation = receive<ImageTransformation>()
        imageService.openImage(path, camera, transformation, this)
    }

    private fun openImagesOnDesktop(ctx: RoutingContext) = with(ctx.call) {
        val paths = queryParameters.getAll(PATH).notNull().notEmpty().map { Base64.getUrlDecoder().decode(it).decodeToString() }.map(Path::of)
        imageService.openImageOnDesktop(paths)
    }

    private fun closeImage(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path()
        imageService.closeImage(path)
    }

    private suspend fun saveImageAs(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path()
        val save = receive<SaveImage>()
        imageService.saveImageAs(path, save)
    }

    private suspend fun analyze(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path().exists()
        respondNullable(imageService.analyze(path))
    }

    private suspend fun annotations(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path().exists()
        val request = receive<AnnotateImageRequest>()
        val location = location(mapper)
        respond(imageService.annotations(path, request, location))
    }

    private suspend fun coordinateInterpolation(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path().exists()
        respondNullable(imageService.coordinateInterpolation(path))
    }

    private suspend fun statistics(ctx: RoutingContext) = with(ctx.call) {
        val path = queryParameters[PATH].notNull().path().exists()
        val transformation = receive<ImageTransformation>()
        val channel = queryParameters[CHANNEL]?.enumOf<ImageChannel>() ?: ImageChannel.GRAY
        val camera = queryParameters[CAMERA]?.ifBlank { null }?.let(connectionService::camera)
        respond(imageService.statistics(path, transformation, channel, camera))
    }

    private suspend fun fovCameras(ctx: RoutingContext) = with(ctx.call) {
        respondBytes(ContentType.Application.Json) { imageService.fovCameras }
    }

    private suspend fun fovTelescopes(ctx: RoutingContext) = with(ctx.call) {
        respondBytes(ContentType.Application.Json) { imageService.fovTelescopes }
    }

    companion object {

        private const val PATH = "path"
        private const val CAMERA = "camera"
        private const val CHANNEL = "channel"
    }
}
