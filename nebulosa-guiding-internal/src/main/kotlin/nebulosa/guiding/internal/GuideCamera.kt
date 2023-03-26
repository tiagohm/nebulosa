package nebulosa.guiding.internal

import nebulosa.imaging.Image

interface GuideCamera {

    val connected: Boolean

    val binning: Int

    val image: Image

    val pixelScale: Double

    val exposure: Long

    fun capture(duration: Long)
}
