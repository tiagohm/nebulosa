package nebulosa.api.image

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.beans.converters.indi.DeviceOrEntityParam
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.transformation.ProtectionMethod
import nebulosa.indi.device.camera.Camera
import nebulosa.star.detection.ImageStar
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@RestController
@RequestMapping("image")
class ImageController(
    private val imageService: ImageService,
) {

    @GetMapping
    fun openImage(
        @RequestParam path: Path,
        @DeviceOrEntityParam(required = false) camera: Camera?,
        @RequestParam(required = false, defaultValue = "true") debayer: Boolean,
        @RequestParam(required = false, defaultValue = "false") calibrate: Boolean,
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
    ) = imageService.openImage(
        path, camera,
        debayer, calibrate, autoStretch, shadow, highlight, midtone,
        mirrorHorizontal, mirrorVertical, invert,
        scnrEnabled, scnrChannel, scnrAmount, scnrProtectionMode,
        output,
    )

    @DeleteMapping
    fun closeImage(@RequestParam path: Path) {
        return imageService.closeImage(path)
    }

    @PutMapping("save-as")
    fun saveImageAs(@RequestParam inputPath: Path, @RequestParam outputPath: Path) {
        imageService.saveImageAs(inputPath, outputPath)
    }

    @GetMapping("annotations")
    fun annotationsOfImage(
        @RequestParam path: Path,
        @RequestParam(required = false, defaultValue = "true") stars: Boolean,
        @RequestParam(required = false, defaultValue = "true") dsos: Boolean,
        @RequestParam(required = false, defaultValue = "false") minorPlanets: Boolean,
        @RequestParam(required = false, defaultValue = "12.0") minorPlanetMagLimit: Double,
    ) = imageService.annotations(path, stars, dsos, minorPlanets, minorPlanetMagLimit)

    @GetMapping("coordinate-interpolation")
    fun coordinateInterpolation(@RequestParam path: Path): CoordinateInterpolation? {
        return imageService.coordinateInterpolation(path)
    }

    @PutMapping("detect-stars")
    fun detectStars(@RequestParam path: Path): List<ImageStar> {
        return imageService.detectStars(path)
    }
}
