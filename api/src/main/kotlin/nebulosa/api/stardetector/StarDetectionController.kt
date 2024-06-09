package nebulosa.api.stardetector

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
        @RequestBody @Valid body: StarDetectionRequest
    ) = starDetectionService.detectStars(path, body)
}
