package nebulosa.alignment.polar

import nebulosa.imaging.Image

interface PolarAlignment<out T> {

    fun align(image: Image): T
}
