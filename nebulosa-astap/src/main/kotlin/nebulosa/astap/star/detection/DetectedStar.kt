package nebulosa.astap.star.detection

import nebulosa.star.detection.ImageStar

data class DetectedStar(
    override val x: Double = 0.0,
    override val y: Double = 0.0,
    override val hfd: Double = 0.0,
    val snr: Double = 0.0,
    val flux: Double = 0.0,
) : ImageStar
