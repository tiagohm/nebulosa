package nebulosa.guiding.internal

import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

internal class GuideGraph(
    private val guider: MultiStarGuider,
    private val maxLength: Int,
) : LinkedList<GuideStats>() {

    private data class TrendLineAccum(
        @JvmField var sumY: Double = 0.0,
        @JvmField var sumXY: Double = 0.0,
        @JvmField var sumY2: Double = 0.0,
    )

    private val trendLineAccum = Array(4) { TrendLineAccum() }
    private val noDitherDEC = WindowedAxisStats(maxLength)
    private val noDitherRA = WindowedAxisStats(maxLength)
    private var raSameSides = 0
    private var ditherStarted = false
    private var peakRA = 0
    private var peakDEC = 0
    private var starLostCnt = 0
    private val timeBase = System.currentTimeMillis()

    fun add(
        offset: GuiderOffset,
        xDuration: Int, yDuration: Int,
        xDirection: GuideDirection, yDirection: GuideDirection,
    ): GuideStats {
        val nr = size

        val oldest = firstOrNull() ?: GuideStats.EMPTY

        trendLineAccum[0].update(nr, maxLength, offset.camera.x, oldest.dx)
        trendLineAccum[1].update(nr, maxLength, offset.camera.y, oldest.dy)
        trendLineAccum[2].update(nr, maxLength, offset.mount.x, oldest.ra)
        trendLineAccum[3].update(nr, maxLength, offset.mount.y, oldest.dec)

        // Update counter for osc index.
        if (nr >= 1) {
            if (offset.mount.x * last.ra > 0.0) {
                raSameSides++
            }

            if (nr >= maxLength) {
                if (this[0].ra * this[1].ra > 0.0) {
                    raSameSides--
                }
            }
        }

        if (!guider.isSettling) {
            val deltaTime = (System.currentTimeMillis() - timeBase).toDouble()
            noDitherRA.add(deltaTime, offset.mount.x, xDuration.toDouble())
            noDitherDEC.add(deltaTime, offset.mount.y, yDuration.toDouble())
        }

        if (nr >= maxLength) {
            removeFirst()
        }

        val entry = GuideStats(
            timestamp = System.currentTimeMillis(),
            dx = offset.camera.x, dy = offset.camera.y,
            ra = offset.mount.x, dec = offset.mount.y,
            raDuration = xDuration, decDuration = yDuration,
            raDirection = xDirection, decDirection = yDirection,
            rmsRA = noDitherRA.populationSigma,
            rmsDEC = noDitherDEC.populationSigma,
            peakRA = max(noDitherRA.maxDisplacement, noDitherRA.minDisplacement),
            peakDEC = max(noDitherDEC.maxDisplacement, noDitherDEC.minDisplacement),
        )

        add(entry)

        return entry
    }

    fun reset() {
        clear()
        noDitherDEC.clear()
        noDitherRA.clear()
        raSameSides = 0
        ditherStarted = false
        peakRA = 0
        peakDEC = 0
        starLostCnt = 0
        trendLineAccum.forEach { it.reset() }
    }

    companion object {

        @JvmStatic
        private fun TrendLineAccum.update(nr: Int, maxNR: Int, value: Double, prevValue: Double) {
            if (nr < maxNR) {
                // Number of items is increasing, increment sums.
                sumY += value
                sumXY += nr * value
                sumY2 += value * value
            } else {
                // Number of items has reached limit. Update counters to reflect
                // removal of oldest value and addition of new value.
                sumXY += (maxNR - 1) * value + prevValue - sumY
                sumY += value - prevValue
                sumY2 += value * value - prevValue * prevValue
            }
        }

        @JvmStatic
        private fun TrendLineAccum.rms(nr: Int): Double {
            if (nr == 0) return 0.0
            return sqrt(nr * sumY2 - sumY * sumY) / nr
        }

        @JvmStatic
        private fun TrendLineAccum.reset() {
            sumY = 0.0
            sumXY = 0.0
            sumY2 = 0.0
        }
    }
}
