package nebulosa.api.image

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import nebulosa.api.atlas.Location
import nebulosa.api.beans.converters.device.DeviceOrEntityParam
import nebulosa.api.beans.converters.location.LocationParam
import nebulosa.indi.device.camera.Camera
import nebulosa.star.detection.ImageStar
import org.hibernate.validator.constraints.Range
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@RestController
@RequestMapping("image")
class ImageController(
    private val imageService: ImageService,
) {

    @PostMapping
    fun openImage(
        @RequestParam path: Path,
        @DeviceOrEntityParam(required = false) camera: Camera?,
        @RequestBody transformation: ImageTransformation,
        output: HttpServletResponse,
    ) = imageService.openImage(path, camera, transformation, output)

    @DeleteMapping
    fun closeImage(@RequestParam path: Path) {
        return imageService.closeImage(path)
    }

    @PutMapping("save-as")
    fun saveImageAs(
        @RequestParam path: Path,
        @DeviceOrEntityParam(required = false) camera: Camera?,
        @RequestBody save: ImageSave
    ) {
        imageService.saveImageAs(path, save, camera)
    }

    @GetMapping("annotations")
    fun annotationsOfImage(
        @RequestParam path: Path,
        @RequestParam(required = false, defaultValue = "true") starsAndDSOs: Boolean,
        @RequestParam(required = false, defaultValue = "false") minorPlanets: Boolean,
        @RequestParam(required = false, defaultValue = "12.0") minorPlanetMagLimit: Double,
        @LocationParam location: Location? = null,
    ) = imageService.annotations(path, starsAndDSOs, minorPlanets, minorPlanetMagLimit, location)

    @GetMapping("coordinate-interpolation")
    fun coordinateInterpolation(@RequestParam path: Path): CoordinateInterpolation? {
        return imageService.coordinateInterpolation(path)
    }

    @PutMapping("detect-stars")
    fun detectStars(@RequestParam path: Path): List<ImageStar> {
        return imageService.detectStars(path)
    }

    @GetMapping("histogram")
    fun histogram(
        @RequestParam path: Path,
        @RequestParam(required = false, defaultValue = "16") @Valid @Range(min = 8, max = 16) bitLength: Int,
    ) = imageService.histogram(path, bitLength)

    @GetMapping("fov-cameras")
    fun fovCameras(response: HttpServletResponse) {
        response.addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        return response.outputStream.write(imageService.fovCameras)
    }

    @GetMapping("fov-telescopes")
    fun fovTelescopes(response: HttpServletResponse) {
        response.addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        return response.outputStream.write(imageService.fovTelescopes)
    }
}
