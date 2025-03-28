package nebulosa.image.algorithms.transformation.adjustment

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min

/**
 * Exposure adjustments controls the overall brightness and range of an image,
 * similar to a camera's exposure setting.
 */
data class Exposure(private val value: Float = 0f) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (value != 0f) {
            val e = 1f + value

            for (i in source.red.indices) {
                source.red[i] = max(0f, min(source.red[i] * e, 1f))

                if (!source.mono) {
                    source.green[i] = max(0f, min(source.green[i] * e, 1f))
                    source.blue[i] = max(0f, min(source.blue[i] * e, 1f))
                }
            }
        }

        return source
    }
}
