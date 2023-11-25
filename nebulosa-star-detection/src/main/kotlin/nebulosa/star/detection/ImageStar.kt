package nebulosa.star.detection

import kotlin.math.sqrt

interface ImageStar {

    val x: Int

    val y: Int

    val hfd: Double

    val snr: Double

    val flux: Double

    fun distance(star: ImageStar): Double {
        val deltaX = x - star.x
        val deltaY = y - star.y
        return sqrt((deltaX * deltaX + deltaY + deltaY).toDouble())
    }
}
