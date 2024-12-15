package nebulosa.image.algorithms.transformation.adjustment

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min

/**
 * Fade reduces the contrast and blends the image with white.
 */
data class Fade(private val value: Float = 0f) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (value != 0f) {
            val a = (1f - value)

            for (i in source.red.indices) {
                source.red[i] = max(0f, min(a * source.red[i] + value, 1f))

                if (!source.mono) {
                    source.green[i] = max(0f, min(a * source.green[i] + value, 1f))
                    source.blue[i] = max(0f, min(a * source.blue[i] + value, 1f))
                }
            }
        }

        return source
    }
}
