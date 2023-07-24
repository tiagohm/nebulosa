package nebulosa.api.controllers

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import nebulosa.api.data.enums.HipsSurveyType
import nebulosa.api.services.ImageService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*

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
    ): String {
        return imageService.frame(
            Angle.from(rightAscension, true), Angle.from(declination),
            width, height, fov.deg, rotation.deg, hipsSurvey,
        ).toString()
    }
}
