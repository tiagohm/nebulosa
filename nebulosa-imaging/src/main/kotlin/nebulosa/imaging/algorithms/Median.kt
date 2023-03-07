package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max

// TODO: Median by indiviual channel.
object Median : ComputationAlgorithm<Float> {

    const val MAX_SAMPLES = 500000

    override fun compute(source: Image): Float {
        val buffer = IntArray(65536)

        val size = source.width * source.height
        val sampleBy = max(1, size / MAX_SAMPLES)
        val sizeOverTwo = size / 2 / sampleBy

        for (i in 0 until size step sampleBy) {
            val c = (source.readGray(i) * 65535f).toInt()
            buffer[c]++
        }

        var amount = 0
        var c = 0

        while (amount < sizeOverTwo) {
            amount += buffer[c++]
        }

        return c / 65535f
    }
}
