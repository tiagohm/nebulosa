package nebulosa.guiding.local

import nebulosa.imaging.Image

interface GuideCamera {

    val image: Image

    val pixelScale: Double

    val exposure: Int

    val autoExposure: Boolean

    fun capture(duration: Long)
}
