package nebulosa.image.algorithms.computation

import nebulosa.image.Image
import nebulosa.image.Image.Companion.forEach
import nebulosa.image.ImageChannel
import nebulosa.image.algorithms.ComputationAlgorithm
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
        @JvmField val count: Int = 0,
        @JvmField val maxCount: Int = 0,
        @JvmField val mean: Float = 0f,
        @JvmField val sumOfSquares: Float = 0f,
        @JvmField val median: Float = 0f,
        @JvmField val variance: Float = 0f,
        @JvmField val stdDev: Float = 0f,
        @JvmField val avgDev: Float = 0f,
        @JvmField val minimum: Float = 0f,
        @JvmField val maximum: Float = 0f,
        @JvmField val histogram: IntArray = IntArray(0),
    ) : ClosedFloatingPointRange<Float> {

        override val start
            get() = minimum

        override val endInclusive
            get() = maximum

        override fun lessThanOrEquals(a: Float, b: Float) = a <= b

        override fun contains(value: Float) = value in minimum..maximum

        override fun isEmpty() = minimum > maximum

        override fun toString() = "Data(count=$count, maxCount=$maxCount, mean=$mean, sumOfSquares=$sumOfSquares, median=$median," +
                " variance=$variance, stdDev=$stdDev, avgDev=$avgDev, minimum=$minimum, maximum=$maximum)"

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
