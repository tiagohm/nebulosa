package nebulosa.api.framing

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import nebulosa.api.image.ImageService
import nebulosa.math.deg
import nebulosa.math.hours
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("framing")
class FramingController(
    private val imageService: ImageService,
    private val framingService: FramingService,
) {

    @GetMapping("hips-surveys")
    fun hipsSurveys() = framingService.availableHipsSurveys

    @PutMapping
    fun frame(
        @RequestParam @Valid @NotBlank rightAscension: String,
        @RequestParam @Valid @NotBlank declination: String,
        @RequestParam(required = false, defaultValue = "1280") @Valid @Range(min = 1, max = 7680) width: Int,
        @RequestParam(required = false, defaultValue = "720") @Valid @Range(min = 1, max = 4320) height: Int,
        @RequestParam(required = false, defaultValue = "1.0") @Valid @Positive @Max(90) fov: Double,
        @RequestParam(required = false, defaultValue = "0.0") rotation: Double,
        @RequestParam(required = false, defaultValue = "CDS/P/DSS2/COLOR") hipsSurvey: String,
    ) = imageService.frame(rightAscension.hours, declination.deg, width, height, fov.deg, rotation.deg, hipsSurvey)
}
