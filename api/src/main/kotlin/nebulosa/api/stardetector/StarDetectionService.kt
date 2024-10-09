package nebulosa.api.stardetector

import nebulosa.stardetector.StarPoint
import java.nio.file.Path

class StarDetectionService {

    fun detectStars(path: Path, options: StarDetectionRequest): List<StarPoint> {
        val starDetector = options.get()
        return starDetector.detect(path)
    }
}
