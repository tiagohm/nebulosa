package nebulosa.imaging.algorithms.star.detection

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Histogram

class DefaultStarDetector : StarDetector {

    private val histogram = Histogram()

    override fun detectStars(image: Image) {
        histogram.compute(image)
    }
}
