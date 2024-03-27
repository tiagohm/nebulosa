package nebulosa.image.algorithms.computation

import nebulosa.image.Image
import nebulosa.image.Image.Companion.forEach
import nebulosa.image.algorithms.ComputationAlgorithm
import nebulosa.image.format.ImageChannel
import kotlin.math.abs
import kotlin.math.min

/**
 * Finds the Median deviation: 1.4826 * median of abs(pixel - [median]).
 *
 * It is less sensitive to outliers compared to the standard deviation.
 */
@Suppress("FloatingPointLiteralPrecision")
class MedianAbsoluteDeviation(
    private var median: Float = Float.NaN,
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val normalized: Boolean = false,
) : ComputationAlgorithm<Float> {

    override fun compute(source: Image): Float {
        val histogram = IntArray(65536)

        if (!median.isFinite()) {
            median = Median(channel).compute(source)
        }

        val totalCount = source.forEach(channel) {
            val value = (abs(it - median) * 65535).toInt()
            histogram[min(value, 65535)]++
        }

        val mad = Median.compute(histogram, totalCount / 2f)
        return if (normalized) STANDARD_DEVIATION_SCALE * mad else mad
    }

    companion object {

        const val STANDARD_DEVIATION_SCALE = 1.482602218505602f
    }
}
