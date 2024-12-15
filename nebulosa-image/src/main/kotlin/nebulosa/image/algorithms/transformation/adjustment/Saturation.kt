package nebulosa.image.algorithms.transformation.adjustment

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min

/**
 * Saturation adjustment changes the intensity of colors while preserving brightness.
 */
data class Saturation(private val value: Float = 0f) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (!source.mono && value != 0f) {
            val s = 1f + value

            for (i in source.red.indices) {
                val l = 0.2990f * source.red[i] + 0.5870f * source.green[i] + 0.1140f * source.blue[i]

                source.red[i] = max(0f, min(l + s * (source.red[i] - l), 1f))
                source.green[i] = max(0f, min(l + s * (source.green[i] - l), 1f))
                source.blue[i] = max(0f, min(l + s * (source.blue[i] - l), 1f))
            }
        }

        return source
    }
}
