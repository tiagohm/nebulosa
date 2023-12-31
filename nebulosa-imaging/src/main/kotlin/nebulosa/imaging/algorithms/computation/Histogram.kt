package nebulosa.imaging.algorithms.computation

import nebulosa.imaging.Image
import nebulosa.imaging.Image.Companion.forEach
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ComputationAlgorithm
import kotlin.math.max
import kotlin.math.min

data class Histogram(
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val bitLength: Int = 16,
) : ComputationAlgorithm<IntArray> {

    init {
        require(bitLength in 8..21) { "invalid bit length: $bitLength" }
    }

    override fun compute(source: Image): IntArray {
        val data = IntArray(2 shl (bitLength - 1))
        val maxValue = data.size - 1

        source.forEach(channel) {
            val value = max(0, min((it * maxValue).toInt(), maxValue))
            data[value]++
        }

        return data
    }
}
