package nebulosa.star.detection

import java.nio.file.Path

interface StarDetector {

    fun detectStars(path: Path): Collection<DetectedStar>
}
