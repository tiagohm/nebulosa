package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ComputationAlgorithm
import nebulosa.imaging.algorithms.TransformAlgorithm
import nebulosa.imaging.algorithms.computation.Statistics
import kotlin.math.max
import kotlin.math.min

data class SigmaClip(
    private val sigma: Double = 3.0,
    private val sigmaLower: Double = sigma,
    private val sigmaUpper: Double = sigma,
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val centerMethod: CenterMethod = CenterMethod.MEDIAN,
    private val maxIteration: Int = 5,
    private val replaceRejectedPixels: Boolean = false,
    private val rejectedPixelFillValue: Float = 0f,
    private val noStatistics: Boolean = true,
) : TransformAlgorithm, ComputationAlgorithm<SigmaClip.Data> {

    enum class CenterMethod {
        MEAN,
        MEDIAN,
    }

    data class Data(
        val count: Int,
        val minBound: Float,
        val maxBound: Float,
        val numberOfIterations: Int,
        val statistics: Statistics.Data,
    )

    override fun transform(source: Image) = source.also(::compute)

    override fun compute(source: Image): Data {
        var minBound = 1f
        var maxBound = 0f
        var totalCount = 0
        var numberOfIterations = 0

        while (maxIteration <= 0 || numberOfIterations < maxIteration) {
            val stats = Statistics(channel, noMedian = centerMethod != CenterMethod.MEDIAN)
                .compute(source)

            val center = if (centerMethod == CenterMethod.MEDIAN) stats.median else stats.mean
            val std = stats.stdDev
            var count = 0

            for (i in 0 until source.size) {
                val pixel = source.read(i, channel)
                val reject = pixel < center - (sigmaLower * std) ||
                        pixel > center + (sigmaUpper * std)

                if (reject) {
                    minBound = min(pixel, minBound)
                    maxBound = max(pixel, maxBound)
                    source.write(i, channel, -pixel)
                    count++
                }
            }

            if (count == 0) break

            totalCount += count
            numberOfIterations++
        }

        val stats = if (noStatistics) Statistics.Data.EMPTY else Statistics(channel).compute(source)

        for (i in 0 until source.size) {
            val pixel = source.read(i, channel)

            if (pixel < 0) {
                if (replaceRejectedPixels) {
                    source.write(i, channel, rejectedPixelFillValue)
                } else {
                    source.write(i, channel, -pixel)
                }
            }
        }

        return Data(totalCount, minBound, maxBound, numberOfIterations, stats)
    }
}
