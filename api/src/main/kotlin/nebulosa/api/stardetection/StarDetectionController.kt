package nebulosa.api.stardetection

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.nio.file.Path

@Validated
@RestController
@RequestMapping("star-detection")
class StarDetectionController(private val starDetectionService: StarDetectionService) {

    @PutMapping
    fun detectStars(
        @RequestParam path: Path,
        @RequestBody @Valid body: StarDetectionOptions
    ) = starDetectionService.detectStars(path, body)
}
