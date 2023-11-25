package nebulosa.imaging.hfd

import nebulosa.star.detection.ImageStar

data class Star(
    override val x: Int, override val y: Int,
    override val hfd: Double = 0.0,
    override val snr: Double = 0.0,
    override val flux: Double = 0.0,
) : ImageStar
