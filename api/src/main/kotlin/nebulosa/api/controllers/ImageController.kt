package nebulosa.api.controllers

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.services.ImageService
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import org.apache.commons.codec.binary.Hex
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import java.util.*

@RestController
class ImageController(
    private val imageService: ImageService,
) {

    @GetMapping("image")
    fun image(
        @RequestParam hash: String,
        @RequestParam(required = false, defaultValue = "true") debayer: Boolean,
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
            Path.of(Hex.decodeHex(hash).decodeToString()),
            debayer, autoStretch, shadow, highlight, midtone,
            mirrorHorizontal, mirrorVertical, invert,
            scnrEnabled, scnrChannel, scnrAmount, scnrProtectionMode,
            output,
        )
    }

    @GetMapping("imagesOfCamera")
    fun imagesOfCamera(@RequestParam name: String): List<SavedCameraImageEntity> {
        return imageService.imagesOfCamera(name)
    }

    @GetMapping("latestImageOfCamera")
    fun latestImageOfCamera(@RequestParam name: String): SavedCameraImageEntity {
        return imageService.latestImageOfCamera(name)
    }
}
