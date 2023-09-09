package nebulosa.api.image

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.connection.ConnectionService
import nebulosa.api.data.enums.PlateSolverType
import nebulosa.api.data.responses.CalibrationResponse
import nebulosa.api.data.responses.ImageAnnotationResponse
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.math.Angle
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Path

@RestController
class ImageController(
    private val connectionService: ConnectionService,
    private val imageService: ImageService,
) {

    @GetMapping("openImage")
    fun openImage(
        @RequestParam @Valid @NotBlank path: String,
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
        imageService.openImage(
            ImageToken.of(path),
            debayer, autoStretch, shadow, highlight, midtone,
            mirrorHorizontal, mirrorVertical, invert,
            scnrEnabled, scnrChannel, scnrAmount, scnrProtectionMode,
            output,
        )
    }

    @PostMapping("closeImage")
    fun closeImage(@RequestParam @Valid @NotBlank path: String) {
        return imageService.closeImage(ImageToken.of(path))
    }

    @PostMapping("saveImageAs")
    fun saveImageAs(
        @RequestParam @Valid @NotBlank inputPath: String,
        @RequestParam @Valid @NotBlank outputPath: String,
    ) {
        imageService.saveImageAs(Path.of(inputPath), Path.of(outputPath))
    }

    @GetMapping("annotationsOfImage")
    fun annotationsOfImage(
        @RequestParam @Valid @NotBlank path: String,
        @RequestParam(required = false, defaultValue = "true") stars: Boolean,
        @RequestParam(required = false, defaultValue = "true") dsos: Boolean,
        @RequestParam(required = false, defaultValue = "false") minorPlanets: Boolean,
        @RequestParam(required = false, defaultValue = "12.0") minorPlanetMagLimit: Double,
    ): List<ImageAnnotationResponse> {
        return imageService.annotations(ImageToken.of(path), stars, dsos, minorPlanets, minorPlanetMagLimit)
    }

    @PostMapping("solveImage")
    fun solveImage(
        @RequestParam @Valid @NotBlank path: String,
        @RequestParam(required = false, defaultValue = "ASTROMETRY_NET_ONLINE") type: PlateSolverType,
        @RequestParam(required = false, defaultValue = "true") blind: Boolean,
        @RequestParam(required = false, defaultValue = "0.0") centerRA: String,
        @RequestParam(required = false, defaultValue = "0.0") centerDEC: String,
        @RequestParam(required = false, defaultValue = "8.0") radius: String,
        @RequestParam(required = false, defaultValue = "1") downsampleFactor: Int,
        @RequestParam(required = false, defaultValue = "") pathOrUrl: String,
        @RequestParam(required = false, defaultValue = "") apiKey: String,
    ): CalibrationResponse {
        return imageService.solveImage(
            ImageToken.of(path), type, blind,
            Angle.from(centerRA, true), Angle.from(centerDEC), Angle.from(radius),
            downsampleFactor, pathOrUrl, apiKey,
        )
    }

    @PostMapping("pointMountHere")
    fun pointMountHere(
        @RequestParam @Valid @NotBlank name: String,
        @RequestParam @Valid @NotBlank path: String,
        @RequestParam @Valid @PositiveOrZero x: Double,
        @RequestParam @Valid @PositiveOrZero y: Double,
        @RequestParam(required = false, defaultValue = "true") synchronized: Boolean,
    ) {
        val mount = requireNotNull(connectionService.mount(name))
        imageService.pointMountHere(mount, ImageToken.of(path), x, y, synchronized)
    }
}
