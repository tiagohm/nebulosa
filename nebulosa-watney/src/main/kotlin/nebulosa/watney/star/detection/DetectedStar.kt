package nebulosa.watney.star.detection

import nebulosa.star.detection.ImageStar

data class DetectedStar(
    override val x: Double = 0.0, override val y: Double = 0.0,
    override var hfd: Double = 0.0,
    val brightness: Double = 0.0,
    val size: Double = 0.0,
) : ImageStar
