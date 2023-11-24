package nebulosa.astap.star.detection

import nebulosa.star.detection.ImageStar

data class DetectedStar(
    override val x: Int = 0,
    override val y: Int = 0,
    override val hfd: Float = 0f,
    override val snr: Float = 0f,
    override val flux: Float = 0f,
) : ImageStar
