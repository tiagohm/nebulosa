package nebulosa.api.stardetection

import nebulosa.star.detection.ImageStar
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class StarDetectionService {

    fun detectStars(path: Path, options: StarDetectionOptions): List<ImageStar> {
        val starDetector = options.get()
        return starDetector.detect(path)
    }
}
