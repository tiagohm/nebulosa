package nebulosa.image.algorithms.computation

import nebulosa.image.Image
import nebulosa.image.Image.Companion.forEach
import nebulosa.image.ImageChannel
import nebulosa.image.algorithms.ComputationAlgorithm
import kotlin.math.max
import kotlin.math.min

class Median(private val channel: ImageChannel = ImageChannel.GRAY) : ComputationAlgorithm<Float> {

    override fun compute(source: Image): Float {
        val histogram = IntArray(65536)

        val totalCount = source.forEach(channel) {
            val value = max(0, min((it * 65535).toInt(), 65535))
            histogram[value]++
        }

        return compute(histogram, totalCount / 2f)
    }

    companion object {

        @JvmStatic
        fun compute(histogram: IntArray, percentileCount: Float): Float {
            val percentileCountInt = percentileCount.toInt()
            var cumulativeFrequency = 0
            var lowerCumulativeFrequency = 0
            var c = 0

            while (cumulativeFrequency <= percentileCountInt && c < histogram.size) {
                lowerCumulativeFrequency = cumulativeFrequency
                cumulativeFrequency += histogram[c++]
            }

            val lowerBound = (c - 1) / 65535f
            val upperBound = c / 65535f
            val binWidth = upperBound - lowerBound
            val percentage = (percentileCount - lowerCumulativeFrequency) / histogram[c - 1]

            return lowerBound + percentage * binWidth
        }
    }
}
