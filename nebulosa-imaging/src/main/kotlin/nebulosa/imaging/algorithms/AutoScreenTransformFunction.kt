package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.log.loggerFor
import kotlin.math.max
import kotlin.math.min

object AutoScreenTransformFunction : ComputationAlgorithm<ScreenTransformFunction.Parameters>, TransformAlgorithm {

    @JvmStatic private val MEDIAN = Median()

    override fun compute(source: Image): ScreenTransformFunction.Parameters {
        // Find the median sample.
        val median = MEDIAN.compute(source)
        // Find the Median deviation: 1.4826 * median of abs(sample[i] - median).
        val medianDeviation = MedianDeviation(median).compute(source)
        // Compute parameters.
        val upperHalf = median > 0.5

        val shadow = if (upperHalf || medianDeviation == 0f) 0f
        else min(1f, max(0f, (median - 2.8f * medianDeviation)))

        val highlight = if (!upperHalf || medianDeviation == 0f) 1f
        else min(1f, max(0f, (median + 2.8f * medianDeviation)))

        val x = if (!upperHalf) median - shadow else 0.25f
        val m = if (!upperHalf) 0.25f else highlight - median

        val midtone = when (x) {
            0f -> 0f
            m -> 0.5f
            1f -> 1f
            else -> ((m - 1) * x) / ((2 * m - 1) * x - m)
        }

        LOG.info("STF auto stretch. midtone={}, shadow={}, highlight={}", midtone, shadow, highlight)

        return ScreenTransformFunction.Parameters(midtone, shadow, highlight)
    }

    override fun transform(source: Image): Image {
        return ScreenTransformFunction(compute(source)).transform(source)
    }

    @JvmStatic private val LOG = loggerFor<AutoScreenTransformFunction>()
}
