package nebulosa.api.framing

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import nebulosa.api.image.ImageService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Path

@RestController
class FramingController(
    private val imageService: ImageService,
) {

    @GetMapping("hipsSurveys")
    fun hipsSurveys(): List<HipsSurvey> {
        return HipsSurveyType.entries.map { it.hipsSurvey }
    }

    @PostMapping("frame")
    fun frame(
            @RequestParam @Valid @NotBlank rightAscension: String,
            @RequestParam @Valid @NotBlank declination: String,
            @RequestParam(required = false, defaultValue = "1280") @Valid @Range(min = 1, max = 7680) width: Int,
            @RequestParam(required = false, defaultValue = "720") @Valid @Range(min = 1, max = 4320) height: Int,
            @RequestParam(required = false, defaultValue = "1.0") @Valid @Positive @Max(90) fov: Double,
            @RequestParam(required = false, defaultValue = "0.0") rotation: Double,
            @RequestParam(required = false, defaultValue = "CDS_P_DSS2_COLOR") hipsSurvey: HipsSurveyType,
    ): Path {
        return imageService.frame(rightAscension.hours, declination.deg, width, height, fov.deg, rotation.deg, hipsSurvey)
    }
}
