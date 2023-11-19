package nebulosa.watney.star.detection

import kotlin.math.hypot

data class ImageStar(
    val x: Float = 0f, val y: Float = 0f,
    val brightness: Float = 0f,
    val size: Float = 0f,
) {

    fun distance(star: ImageStar): Float {
        return hypot(x - star.x, y - star.y)
    }
}
