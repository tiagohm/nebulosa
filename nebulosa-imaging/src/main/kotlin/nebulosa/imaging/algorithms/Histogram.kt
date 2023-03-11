package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.sqrt

class Histogram : ComputationAlgorithm<Boolean> {

    private val buffer = IntArray(65536)

    var median = 0f
        private set

    var peakCount = 0
        private set

    var peakValue = 0f
        private set

    var pixelSum = 0f
        private set

    var pixelAvg = 0f
        private set

    var stdDev = 0f
        private set

    operator fun get(pixel: Int) = buffer[pixel]

    override fun compute(source: Image): Boolean {
        val size = source.width * source.height
        val sizeOverTwo = size / 2

        peakCount = 0
        peakValue = 0f
        pixelSum = 0f
        pixelAvg = 0f

        buffer.fill(0)

        for (i in source.indices) {
            // TODO: Histogram by channel?
            val pixel = source.readGray(i)
            val value = (pixel * 65535f).toInt()
            buffer[value]++

            peakValue = max(peakValue, pixel)
            peakCount = max(peakCount, buffer[value])
            pixelSum += pixel
        }

        pixelAvg = pixelSum / size

        var diffSquared = 0f

        for (i in buffer.indices) {
            val s = (i / 65535f) - pixelAvg
            diffSquared += s * s * buffer[i]
        }

        stdDev = sqrt(diffSquared / size)

        var amount = 0
        var c = 0

        while (amount < sizeOverTwo) {
            amount += buffer[c++]
        }

        median = c / 65535f

        return true
    }
}
