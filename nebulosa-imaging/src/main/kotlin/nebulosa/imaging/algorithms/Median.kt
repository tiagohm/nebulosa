package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import kotlin.math.max

class Median(private val channel: ImageChannel = ImageChannel.GRAY) : ComputationAlgorithm<Float> {

    override fun compute(source: Image): Float {
        val buffer = IntArray(65536)

        val size = source.width * source.height
        val sampleBy = max(1, size / MAX_SAMPLES)
        val sizeOverTwo = size / 2 / sampleBy

        for (i in 0 until size step sampleBy) {
            val c = when (channel) {
                ImageChannel.GRAY -> (source.readGray(i) * 65535f).toInt()
                ImageChannel.RED -> (source.readRed(i) * 65535f).toInt()
                ImageChannel.GREEN -> (source.readGreen(i) * 65535f).toInt()
                else -> (source.readBlue(i) * 65535f).toInt()
            }

            buffer[c]++
        }

        var amount = 0
        var c = 0

        while (amount < sizeOverTwo) {
            amount += buffer[c++]
        }

        return c / 65535f
    }

    companion object {

        const val MAX_SAMPLES = 500000
    }
}
