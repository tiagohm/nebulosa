package nebulosa.api.image

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.http.Controller
import nebulosa.api.http.location
import nebulosa.api.validators.enumOf
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import nebulosa.api.validators.path
import nebulosa.image.format.ImageChannel
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.util.*

class ImageController(
    override val app: Javalin,
    private val imageService: ImageService,
    private val connectionService: ConnectionService,
) : Controller {

    init {
        app.post("image", ::openImage)
        app.post("image/open-on-desktop", ::openImagesOnDesktop)
        app.delete("image", ::closeImage)
        app.put("image/save-as", ::saveImageAs)
        app.put("image/analyze", ::analyze)
        app.put("image/annotations", ::annotations)
        app.get("image/coordinate-interpolation", ::coordinateInterpolation)
        app.post("image/statistics", ::statistics)
        app.get("image/fov-cameras", ::fovCameras)
        app.get("image/fov-telescopes", ::fovTelescopes)
    }

    private fun openImage(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        val camera = ctx.queryParam("camera")?.ifBlank { null }?.let(connectionService::camera)
        val transformation = ctx.bodyAsClass<ImageTransformation>()
        imageService.openImage(path, camera, transformation, ctx.res())
    }

    private fun openImagesOnDesktop(ctx: Context) {
        val paths = ctx.queryParams("path").map { Base64.getUrlDecoder().decode(it).decodeToString() }.map(Path::of)
        imageService.openImageOnDesktop(paths)
    }

    private fun closeImage(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path()
        return imageService.closeImage(path)
    }

    private fun saveImageAs(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path()
        val save = ctx.bodyAsClass<SaveImage>()
        imageService.saveImageAs(path, save)
    }

    private fun analyze(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        imageService.analyze(path)?.also(ctx::json)
    }

    private fun annotations(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        val request = ctx.bodyAsClass<AnnotateImageRequest>()
        val location = ctx.location()
        ctx.json(imageService.annotations(path, request, location))
    }

    private fun coordinateInterpolation(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        imageService.coordinateInterpolation(path)?.also(ctx::json)
    }

    private fun statistics(ctx: Context) {
        val path = ctx.queryParam("path").notNull().path().exists()
        val transformation = ctx.bodyAsClass<ImageTransformation>()
        val channel = ctx.queryParam("channel")?.enumOf<ImageChannel>() ?: ImageChannel.GRAY
        val camera = ctx.queryParam("camera")?.ifBlank { null }?.let(connectionService::camera)
        ctx.json(imageService.statistics(path, transformation, channel, camera))
    }

    private fun fovCameras(ctx: Context) {
        val bytes = imageService.fovCameras
        ctx.writeSeekableStream(ByteArrayInputStream(bytes), "application/json", bytes.size.toLong())
    }

    private fun fovTelescopes(ctx: Context) {
        val bytes = imageService.fovTelescopes
        ctx.writeSeekableStream(ByteArrayInputStream(bytes), "application/json", bytes.size.toLong())
    }
}
