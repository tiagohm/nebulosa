package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.abs
import kotlin.math.max

/**
 * Finds the Median deviation: 1.4826 * median of abs(pixel - [median]).
 */
class MedianDeviation(private val median: Float) : ComputationAlgorithm<Float> {

    override fun compute(source: Image): Float {
        val buffer = IntArray(65536)

        val size = source.width * source.height
        val sampleBy = max(1, size / Median.MAX_SAMPLES)
        val sizeOverTwo = size / 2 / sampleBy

        for (i in 0 until size step sampleBy) {
            val c = source.readGray(i)
            buffer[(abs(median - c) * 65535f).toInt()]++
        }

        var amount = 0
        var c = 0

        while (amount < sizeOverTwo) {
            amount += buffer[c++]
        }

        return 1.4826f * (c / 65535f)
    }
}
