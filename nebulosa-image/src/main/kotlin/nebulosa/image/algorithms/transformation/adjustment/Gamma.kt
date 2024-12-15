package nebulosa.image.algorithms.transformation.adjustment

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Gamma correction adjusts the brightness non-linearly to simulate the response of
 * the human eye or adapt an image for different displays.
 */
data class Gamma(private val value: Float = 0f) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (value != 0f) {
            val g = 1f + value
            val lut = if (g == 0f) FloatArray(0) else FloatArray(65536) { (it / 65535f).pow(g) }

            for (i in source.red.indices) {
                source.red[i] = max(0f, min(source.red[i].gamma(lut), 1f))

                if (!source.mono) {
                    source.green[i] = max(0f, min(source.green[i].gamma(lut), 1f))
                    source.blue[i] = max(0f, min(source.blue[i].gamma(lut), 1f))
                }
            }
        }

        return source
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Float.gamma(lut: FloatArray): Float {
        return if (lut.isEmpty()) 1f else lut[(this * 65535f).toInt()]
    }
}
