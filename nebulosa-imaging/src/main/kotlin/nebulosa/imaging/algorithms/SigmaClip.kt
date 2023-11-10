package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import kotlin.math.max
import kotlin.math.min

data class SigmaClip(
    val sigma: Double = 3.0,
    val sigmaLower: Double = sigma,
    val sigmaUpper: Double = sigma,
    val channel: ImageChannel = ImageChannel.GRAY,
    val centerMethod: CenterMethod = CenterMethod.MEDIAN,
    val maxIteration: Int = 5,
    val replaceRejectedPixels: Boolean = false,
    val rejectedPixelFillValue: Float = 0f,
    val noStatistics: Boolean = true,
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
            val stats = Statistics(channel, noSumOfSquares = true, noMedian = centerMethod != CenterMethod.MEDIAN)
                .compute(source)

            val center = if (centerMethod == CenterMethod.MEDIAN) stats.median else stats.mean
            val std = stats.stdDev
            var count = 0

            for (i in source.indices) {
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

        for (i in source.indices) {
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
