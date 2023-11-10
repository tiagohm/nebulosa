package nebulosa.imaging.algorithms.star.hfd

import nebulosa.imaging.algorithms.star.detection.DetectedImage

data class ImageStar(
    override val x: Double,
    override val y: Double,
    override val mass: Double = 0.0,
    override val snr: Double = 0.0,
    override val hfd: Double = 0.0,
    override val peak: Double = 0.0,
    val result: FindResult = FindResult.ERROR,
) : DetectedImage
