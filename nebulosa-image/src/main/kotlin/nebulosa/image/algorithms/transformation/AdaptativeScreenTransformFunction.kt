package nebulosa.image.algorithms.transformation

import nebulosa.image.Image
import nebulosa.image.algorithms.ComputationAlgorithm
import nebulosa.image.algorithms.TransformAlgorithm
import nebulosa.image.algorithms.computation.Median
import nebulosa.image.algorithms.computation.MedianAbsoluteDeviation
import nebulosa.image.format.ImageChannel
import nebulosa.log.d
import nebulosa.log.loggerFor
import kotlin.math.max
import kotlin.math.min

/**
 * 8.5.7 Adaptive Display Function Algorithm.
 *
 * @see <a href="https://pixinsight.com/doc/docs/XISF-1.0-spec/XISF-1.0-spec.html#__XISF_Data_Objects_:_XISF_Image_:_Adaptive_Display_Function_Algorithm__">Reference</a>
 */
data class AdaptativeScreenTransformFunction(
    private val b: Float = DEFAULT_MEAN_BACKGROUND,
    private val c: Float = DEFAULT_CLIPPING_POINT,
    private val linked: Boolean = false,
) : ComputationAlgorithm<ScreenTransformFunction.Parameters>, TransformAlgorithm {

    override fun compute(source: Image): ScreenTransformFunction.Parameters {
        val channels = if (linked || source.numberOfChannels == 1) listOf(ImageChannel.GRAY) else ImageChannel.RGB
        // Find the median sample.
        val median = FloatArray(channels.size) { Median(channels[it]).compute(source) }
        // Find the Median deviation: 1.4826 * median of abs(sample[i] - median).
        val mad = FloatArray(channels.size) { MedianAbsoluteDeviation(median[it], channels[it], true).compute(source) }
        // Compute parameters.
        val upperHalf = median.sum() > 1.5

        val shadow = FloatArray(channels.size) { if (upperHalf || mad[it] == 0f) 0f else min(1f, max(0f, (median[it] + c * mad[it]))) }
        val highlight = FloatArray(channels.size) { if (!upperHalf || mad[it] == 0f) 1f else min(1f, max(0f, (median[it] - c * mad[it]))) }
        val x = FloatArray(channels.size) { if (!upperHalf) median[it] - shadow[it] else b }
        val m = FloatArray(channels.size) { if (!upperHalf) b else highlight[it] - median[it] }

        val midtone = FloatArray(channels.size) {
            when (x[it]) {
                0f -> 0f
                m[it] -> 0.5f
                1f -> 1f
                else -> ((m[it] - 1) * x[it]) / ((2 * m[it] - 1) * x[it] - m[it])
            }
        }

        return ScreenTransformFunction.Parameters(midtone.average().toFloat(), shadow.average().toFloat(), highlight.average().toFloat())
            .also { LOG.d("STF auto stretch. median={}, params={}", median, it) }
    }

    override fun transform(source: Image): Image {
        return ScreenTransformFunction(compute(source)).transform(source)
    }

    companion object {

        val DEFAULT = AdaptativeScreenTransformFunction()

        const val DEFAULT_MEAN_BACKGROUND = 0.25f
        const val DEFAULT_CLIPPING_POINT = -2.8f

        private val LOG = loggerFor<AdaptativeScreenTransformFunction>()
    }
}
