package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.Image.Companion.forEach
import nebulosa.imaging.ImageChannel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class Statistics(
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val noMedian: Boolean = false,
    private val noDeviation: Boolean = false,
) : ComputationAlgorithm<Statistics.Data> {

    @Suppress("ArrayInDataClass")
    data class Data(
        val count: Int = 0,
        val maxCount: Int = 0,
        val mean: Float = 0f,
        val sumOfSquares: Float = 0f,
        val median: Float = 0f,
        val variance: Float = 0f,
        val stdDev: Float = 0f,
        val avgDev: Float = 0f,
        val minimum: Float = 0f,
        val maximum: Float = 0f,
        val histogram: IntArray = IntArray(0),
    ) : ClosedFloatingPointRange<Float> {

        override val start
            get() = minimum

        override val endInclusive
            get() = maximum

        override fun lessThanOrEquals(a: Float, b: Float) = a <= b

        override fun contains(value: Float): Boolean = value in minimum..maximum

        override fun isEmpty(): Boolean = !(minimum <= maximum)

        companion object {

            @JvmStatic val EMPTY = Data()
        }
    }

    override fun compute(source: Image): Data {
        val data = IntArray(65536)

        var minimum = 1f
        var maximum = 0f
        var maxCount = 0
        var sum = 0f
        var sumOfSquares = 0f
        var variance = 0f

        val totalCount = source.forEach(channel) {
            val value = max(0, min((it * 65535).toInt(), 65535))

            data[value]++

            minimum = min(minimum, it)
            maximum = max(maximum, it)
            maxCount = max(maxCount, data[value])
            sum += it
            sumOfSquares += it * it
        }

        check(totalCount >= 1) { "invalid source. count < 1: $totalCount" }

        val mean = sum / totalCount
        val median = if (noMedian && noDeviation) 0f else Median.compute(data, totalCount / 2f)

        var eps = 0f
        var stdDev = 0f
        var avgDev = 0f

        if (!noDeviation) {
            source.forEach(channel) {
                val d = it - mean
                variance += d * d
                eps += d

                avgDev += abs(it - median)
            }

            variance = (variance - eps * eps / totalCount) / (totalCount - 1)
            stdDev = sqrt(variance)

            avgDev /= totalCount
        }

        return Data(totalCount, maxCount, mean, sumOfSquares, median, variance, stdDev, avgDev, minimum, maximum, data)
    }

    companion object {

        @JvmStatic val GRAY = Statistics()
        @JvmStatic val RED = Statistics(ImageChannel.RED)
        @JvmStatic val GREEN = Statistics(ImageChannel.GREEN)
        @JvmStatic val BLUE = Statistics(ImageChannel.BLUE)
    }
}
