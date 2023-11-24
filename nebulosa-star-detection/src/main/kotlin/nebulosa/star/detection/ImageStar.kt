package nebulosa.star.detection

import kotlin.math.sqrt

interface ImageStar {

    val x: Int

    val y: Int

    val hfd: Float

    val snr: Float

    val flux: Float

    fun distance(star: ImageStar): Float {
        val deltaX = x - star.x
        val deltaY = y - star.y
        return sqrt((deltaX * deltaX + deltaY + deltaY).toFloat())
    }
}
