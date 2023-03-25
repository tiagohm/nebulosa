package nebulosa.guiding.internal

import nebulosa.imaging.Image

interface GuideCamera {

    val image: Image

    val pixelScale: Double

    val exposure: Long

    fun capture(duration: Long)
}
