package nebulosa.imaging.algorithms.star.hfd

data class ImageStar(
    val x: Float,
    val y: Float,
    val mass: Float = 0f,
    val snr: Float = 0f,
    val hfd: Float = 0f,
    val peak: Float = 0f,
    val result: FindResult = FindResult.ERROR,
)
