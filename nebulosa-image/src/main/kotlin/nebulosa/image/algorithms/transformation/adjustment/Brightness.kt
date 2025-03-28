package nebulosa.image.algorithms.transformation.adjustment

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min

/**
 * Brightness adjustment modifies the intensity of all pixels equally.
 */
data class Brightness(private val value: Float = 0f) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (value != 0f) {
            for (i in source.red.indices) {
                source.red[i] = max(0f, min(source.red[i] + value, 1f))

                if (!source.mono) {
                    source.green[i] = max(0f, min(source.green[i] + value, 1f))
                    source.blue[i] = max(0f, min(source.blue[i] + value, 1f))
                }
            }
        }

        return source
    }
}
