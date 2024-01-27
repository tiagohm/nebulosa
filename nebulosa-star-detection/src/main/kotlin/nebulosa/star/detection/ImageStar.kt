package nebulosa.star.detection

import nebulosa.math.Point2D

interface ImageStar : Point2D {

    val hfd: Double

    val snr: Double

    val flux: Double
}
