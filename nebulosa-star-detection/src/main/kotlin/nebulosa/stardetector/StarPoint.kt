package nebulosa.stardetector

import nebulosa.math.Point2D

interface StarPoint : Point2D {

    val hfd: Double

    val snr: Double

    val flux: Double
}
