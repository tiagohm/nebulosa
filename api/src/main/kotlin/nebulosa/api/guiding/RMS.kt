package nebulosa.api.guiding

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

class RMS {

    private var sumRA = 0.0
    private var sumRASquared = 0.0
    private var sumDEC = 0.0
    private var sumDECSquared = 0.0

    var size = 0
        private set

    var rightAscension = 0.0
        private set

    var declination = 0.0
        private set

    var total = 0.0
        private set

    var peakRA = 0.0
        private set

    var peakDEC = 0.0
        private set

    fun addDataPoint(raDistance: Double, decDistance: Double) {
        size++

        sumRA += raDistance
        sumRASquared += raDistance * raDistance
        sumDEC += decDistance
        sumDECSquared += decDistance * decDistance

        peakRA = max(peakRA, abs(raDistance))
        peakDEC = max(peakDEC, abs(decDistance))

        computeRMS()
    }

    fun removeDataPoint(raDistance: Double, decDistance: Double) {
        size--

        sumRA -= raDistance
        sumRASquared -= raDistance * raDistance
        sumDEC -= decDistance
        sumDECSquared -= decDistance * decDistance

        computeRMS()
    }

    private fun computeRMS() {
        rightAscension = sqrt(size * sumRASquared - sumRA * sumRA) / size
        declination = sqrt(size * sumDECSquared - sumDEC * sumDEC) / size
        total = hypot(rightAscension, declination)
    }

    fun clear() {
        size = 0
        sumRA = 0.0
        sumRASquared = 0.0
        sumDEC = 0.0
        sumDECSquared = 0.0
        rightAscension = 0.0
        declination = 0.0
        total = 0.0
        peakRA = 0.0
        peakDEC = 0.0
    }
}
