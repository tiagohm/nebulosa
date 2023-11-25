package nebulosa.imaging.hfd

import nebulosa.star.detection.ImageStar

data class Star(
    override val x: Int, override val y: Int,
    override val hfd: Float = 0f,
    override val snr: Float = 0f,
    override val flux: Float = 0f,
) : ImageStar
