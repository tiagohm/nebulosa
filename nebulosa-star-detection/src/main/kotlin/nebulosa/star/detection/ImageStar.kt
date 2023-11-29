package nebulosa.star.detection

import kotlin.math.hypot

interface ImageStar {

    val x: Double

    val y: Double

    val hfd: Double

    val snr: Double

    val flux: Double

    fun distance(star: ImageStar): Double {
        return hypot(x - star.x, y - star.y)
    }
}
