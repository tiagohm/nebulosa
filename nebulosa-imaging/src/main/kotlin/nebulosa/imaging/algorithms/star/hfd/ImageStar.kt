package nebulosa.imaging.algorithms.star.hfd

data class ImageStar(
    val x: Double,
    val y: Double,
    val mass: Double = 0.0,
    val snr: Double = 0.0,
    val hfd: Double = 0.0,
    val peak: Double = 0.0,
    val result: FindResult = FindResult.ERROR,
)
