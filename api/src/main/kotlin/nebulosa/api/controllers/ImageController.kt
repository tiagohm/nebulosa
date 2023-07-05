package nebulosa.api.controllers

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.SavedCameraImage
import nebulosa.api.services.ImageService
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import java.util.*

@RestController
@RequestMapping("image")
class ImageController(
    private val imageService: ImageService,
) {

    @GetMapping("path/{hash}")
    fun path(
        @PathVariable hash: String,
        @RequestParam(required = false, defaultValue = "false") autoStretch: Boolean,
        @RequestParam(required = false, defaultValue = "0.0") shadow: Float,
        @RequestParam(required = false, defaultValue = "1.0") highlight: Float,
        @RequestParam(required = false, defaultValue = "0.5") midtone: Float,
        @RequestParam(required = false, defaultValue = "false") mirrorHorizontal: Boolean,
        @RequestParam(required = false, defaultValue = "false") mirrorVertical: Boolean,
        @RequestParam(required = false, defaultValue = "false") invert: Boolean,
        @RequestParam(required = false, defaultValue = "false") scnrEnabled: Boolean,
        @RequestParam(required = false, defaultValue = "GREEN") scnrChannel: ImageChannel,
        @RequestParam(required = false, defaultValue = "0.5") scnrAmount: Float,
        @RequestParam(required = false, defaultValue = "AVERAGE_NEUTRAL") scnrProtectionMode: ProtectionMethod,
        output: HttpServletResponse,
    ) {
        imageService.load(
            Path.of(Base64.getUrlDecoder().decode(hash).decodeToString()),
            autoStretch, shadow, highlight, midtone,
            mirrorHorizontal, mirrorVertical, invert,
            scnrEnabled, scnrChannel, scnrAmount, scnrProtectionMode,
            output,
        )
    }

    @GetMapping("path/{hash}/info")
    fun pathInfo(@PathVariable hash: String): SavedCameraImage {
        return imageService.savedImageOfPath(Path.of(Base64.getUrlDecoder().decode(hash).decodeToString()))
    }

    @GetMapping("camera/{name}")
    fun camera(
        @PathVariable name: String,
        @RequestParam(required = false, defaultValue = "false") autoStretch: Boolean,
        @RequestParam(required = false, defaultValue = "0.0") shadow: Float,
        @RequestParam(required = false, defaultValue = "1.0") highlight: Float,
        @RequestParam(required = false, defaultValue = "0.5") midtone: Float,
        @RequestParam(required = false, defaultValue = "false") mirrorHorizontal: Boolean,
        @RequestParam(required = false, defaultValue = "false") mirrorVertical: Boolean,
        @RequestParam(required = false, defaultValue = "false") invert: Boolean,
        @RequestParam(required = false, defaultValue = "false") scnrEnabled: Boolean,
        @RequestParam(required = false, defaultValue = "GREEN") scnrChannel: ImageChannel,
        @RequestParam(required = false, defaultValue = "0.5") scnrAmount: Float,
        @RequestParam(required = false, defaultValue = "AVERAGE_NEUTRAL") scnrProtectionMode: ProtectionMethod,
        output: HttpServletResponse,
    ) {
        imageService.load(
            Path.of(imageService.latestSavedImageOfCamera(name).path),
            autoStretch, shadow, highlight, midtone,
            mirrorHorizontal, mirrorVertical, invert,
            scnrEnabled, scnrChannel, scnrAmount, scnrProtectionMode,
            output,
        )
    }

    @GetMapping("camera/{name}/info")
    fun cameraInfo(@PathVariable name: String): SavedCameraImage {
        return imageService.latestSavedImageOfCamera(name)
    }
}
