package nebulosa.astap.star.detection

import nebulosa.star.detection.ImageStar

data class Star(
    override val x: Double = 0.0,
    override val y: Double = 0.0,
    override val hfd: Double = 0.0,
    override val snr: Double = 0.0,
    override val flux: Double = 0.0,
) : ImageStar
