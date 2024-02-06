package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.ComputationAlgorithm
import nebulosa.imaging.algorithms.TransformAlgorithm
import nebulosa.imaging.algorithms.computation.Median
import nebulosa.imaging.algorithms.computation.MedianAbsoluteDeviation
import nebulosa.log.loggerFor
import kotlin.math.max
import kotlin.math.min

data object AutoScreenTransformFunction : ComputationAlgorithm<ScreenTransformFunction.Parameters>, TransformAlgorithm {

    override fun compute(source: Image): ScreenTransformFunction.Parameters {
        // Find the median sample.
        val median = Median().compute(source)
        // Find the Median deviation: 1.4826 * median of abs(sample[i] - median).
        val mad = MedianAbsoluteDeviation(median, normalized = true).compute(source)
        // Compute parameters.
        val upperHalf = median > 0.5

        val shadow = if (upperHalf || mad == 0f) 0f
        else min(1f, max(0f, (median - 2.8f * mad)))

        val highlight = if (!upperHalf || mad == 0f) 1f
        else min(1f, max(0f, (median + 2.8f * mad)))

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
