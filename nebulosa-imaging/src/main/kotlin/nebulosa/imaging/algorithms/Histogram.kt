package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Histogram : ComputationAlgorithm<Boolean> {

    private val buffer = IntArray(65536)

    var median = 0f
        private set

    var maxCount = 0
        private set

    var maxValue = 0
        private set

    var minValue = 0
        private set

    var pixelSum = 0L
        private set

    var pixelAvg = 0f
        private set

    var stdDev = 0f
        private set

    operator fun get(pixel: Int) = buffer[pixel]

    override fun compute(source: Image): Boolean {
        val size = source.width * source.height
        val sizeOverTwo = size / 2

        maxCount = 0
        maxValue = 0
        minValue = 65535
        pixelSum = 0
        pixelAvg = 0f

        buffer.fill(0)

        for (i in source.indices) {
            // TODO: Histogram by channel?
            val pixel = source.readGray(i)
            val value = (pixel * 65535f).toInt()
            buffer[value]++

            maxValue = max(maxValue, value)
            maxCount = max(maxCount, buffer[value])
            minValue = min(minValue, value)
            pixelSum += value
        }

        pixelAvg = pixelSum.toFloat() / size

        var diffSquared = 0f

        for (i in buffer.indices) {
            val s = i - pixelAvg
            diffSquared += s * s * buffer[i]
        }

        stdDev = sqrt(diffSquared / size)

        var amount = 0
        var c = 0

        while (amount <= sizeOverTwo && c < buffer.size) {
            amount += buffer[c++]
        }

        median = c.toFloat()

        return true
    }
}
