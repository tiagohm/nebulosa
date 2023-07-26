package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ComputationAlgorithm.Companion.sampling

class Median(
    private val channel: ImageChannel = ImageChannel.GRAY,
    private val sampleBy: Int = 1,
) : ComputationAlgorithm<Float> {

    override fun compute(source: Image): Float {
        val buffer = IntArray(65536)

        val length = source.sampling(channel, sampleBy) {
            val value = (it * 65535f).toInt()
            buffer[value]++
        }

        return compute(buffer, length / 2)
    }

    companion object {

        @JvmStatic
        internal fun compute(data: IntArray, length: Int): Float {
            var amount = 0
            var c = 0

            while (amount <= length && c < data.size) {
                amount += data[c++]
            }

            return (c - 1) / 65535f
        }
    }
}
