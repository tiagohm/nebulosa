package nebulosa.watney.star.detection

import nebulosa.star.detection.ImageStar

data class DetectedStar(
    override val x: Int = 0, override val y: Int = 0,
    val size: Double = 0.0,
    override var hfd: Float = 0f,
    override var snr: Float = 0f,
    override var flux: Float = 0f,
) : ImageStar
