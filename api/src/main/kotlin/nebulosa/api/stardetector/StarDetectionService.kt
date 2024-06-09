package nebulosa.api.stardetector

import nebulosa.stardetector.StarPoint
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class StarDetectionService {

    fun detectStars(path: Path, options: StarDetectionRequest): List<StarPoint> {
        val starDetector = options.get()
        return starDetector.detect(path)
    }
}
