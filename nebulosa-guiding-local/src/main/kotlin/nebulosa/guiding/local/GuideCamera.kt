package nebulosa.guiding.local

import nebulosa.imaging.Image

interface GuideCamera {

    val image: Image

    fun capture(duration: Long)
}
