package nebulosa.api.image

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import nebulosa.api.connection.ConnectionService
import nebulosa.api.javalin.*
import java.io.ByteArrayInputStream

class ImageController(
    app: Javalin,
    private val imageService: ImageService,
    private val connectionService: ConnectionService,
) {

    init {
        app.post("image", ::openImage)
        app.delete("image", ::closeImage)
        app.put("image/save-as", ::saveImageAs)
        app.put("image/annotations", ::annotations)
        app.get("image/coordinate-interpolation", ::coordinateInterpolation)
        app.get("image/histogram", ::histogram)
        app.get("image/fov-cameras", ::fovCameras)
        app.get("image/fov-telescopes", ::fovTelescopes)
    }

    private fun openImage(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        val camera = ctx.queryParam("camera")?.ifBlank { null }?.let(connectionService::camera)
        val transformation = ctx.bodyAsClass<ImageTransformation>()
        imageService.openImage(path, camera, transformation, ctx.res())
    }

    private fun closeImage(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        return imageService.closeImage(path)
    }

    private fun saveImageAs(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        val save = ctx.bodyAsClass<SaveImage>()
        imageService.saveImageAs(path, save)
    }

    private fun annotations(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        val request = ctx.bodyAsClass<AnnotateImageRequest>()
        val location = ctx.locationOrNull()
        ctx.json(imageService.annotations(path, request, location))
    }

    private fun coordinateInterpolation(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        imageService.coordinateInterpolation(path)?.also(ctx::json)
    }

    private fun histogram(ctx: Context) {
        val path = ctx.queryParamAsPath("path").exists().get()
        val bitLength = ctx.queryParamAsInt("bitLength").range(8..16).getOrDefault(16)
        ctx.json(imageService.histogram(path, bitLength))
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
