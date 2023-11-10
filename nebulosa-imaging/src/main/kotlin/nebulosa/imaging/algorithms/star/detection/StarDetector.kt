package nebulosa.imaging.algorithms.star.detection

import nebulosa.imaging.Image

interface StarDetector {

    fun detectStars(image: Image): List<DetectedImage>
}
