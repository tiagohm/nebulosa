package nebulosa.image.algorithms.transformation.adjustment

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import nebulosa.image.algorithms.computation.Statistics
import kotlin.math.max
import kotlin.math.min

/**
 * Contrast adjustment modifies the difference between the lightest and darkest areas.
 */
data class Contrast(
    private val value: Float = 0f,
    private val midPoint: Float = 0.5f,
    private val useMean: Boolean = false,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (value != 0f) {
            val mean = if (useMean) STATS.compute(source).mean else midPoint
            val c = 1f + value

            for (i in source.red.indices) {
                source.red[i] = max(0f, min(mean + c * (source.red[i] - mean), 1f))

                if (!source.mono) {
                    source.green[i] = max(0f, min(mean + c * (source.green[i] - mean), 1f))
                    source.blue[i] = max(0f, min(mean + c * (source.blue[i] - mean), 1f))
                }
            }
        }

        return source
    }

    companion object {

        private val STATS = Statistics(noMedian = true, noDeviation = true)
    }
}
