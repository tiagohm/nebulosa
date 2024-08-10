package nebulosa.image.algorithms.computation

import nebulosa.image.Image
import nebulosa.image.Image.Companion.forEach
import nebulosa.image.algorithms.ComputationAlgorithm
import nebulosa.image.format.ImageChannel
import kotlin.math.min

data class Histogram(
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val bitLength: Int = 16,
) : ComputationAlgorithm<IntArray> {

    init {
        require(bitLength in 8..21) { "invalid bit length: $bitLength" }
    }

    override fun compute(source: Image): IntArray {
        val data = IntArray(1 shl bitLength)
        val maxValue = data.size - 1

        source.forEach(channel) {
            val value = min((it * maxValue).toInt(), maxValue)
            data[value]++
        }

        return data
    }
}
