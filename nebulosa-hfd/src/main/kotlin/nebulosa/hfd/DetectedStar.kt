package nebulosa.hfd

import nebulosa.star.detection.ImageStar

data class DetectedStar(
    override val x: Double,
    override val y: Double,
    val mass: Double = 0.0,
    val snr: Double = 0.0,
    override val hfd: Double = 0.0,
    val peak: Double = 0.0,
    val result: FindResult = FindResult.ERROR,
) : ImageStar
