package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ComputationAlgorithm.Companion.sampling
import kotlin.math.abs

/**
 * Finds the Median deviation: 1.4826 * median of abs(pixel - [median]).
 */
@Suppress("FloatingPointLiteralPrecision")
class MedianDeviation(
    private val median: Float,
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val sampleBy: Int = 1,
) : ComputationAlgorithm<Float> {

    override fun compute(source: Image): Float {
        val buffer = IntArray(65536)

        val length = source.sampling(channel, sampleBy) {
            buffer[(abs(median - it) * 65535).toInt()]++
        }

        return 1.482602218505602f * Median.compute(buffer, length / 2)
    }
}
