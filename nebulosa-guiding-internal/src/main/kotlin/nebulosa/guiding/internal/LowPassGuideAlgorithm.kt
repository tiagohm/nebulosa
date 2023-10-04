package nebulosa.guiding.internal

import nebulosa.guiding.GuideAxis
import kotlin.math.abs

data class LowPassGuideAlgorithm(
    override val axis: GuideAxis,
    override var minMove: Double = DEFAULT_MIN_MOVE,
    var slopeWeight: Double = DEFAULT_SLOPE_WEIGHT,
) : GuideAlgorithm {

    private val axisStats = WindowedAxisStats()
    private var timeBase = 0

    init {
        reset()
    }

    override fun compute(input: Double): Double {
        // Manual trimming of window (instead of auto-size)
        // is done for full backward compatibility with original algo.
        axisStats.add((timeBase++).toDouble(), input, 0.0)
        axisStats.removeOldestEntry()

        if (abs(input) < minMove) return 0.0

        val linearFit = axisStats.linearFit()
        val median = axisStats.median
        val ret = median + slopeWeight * linearFit.slope

        return if (abs(ret) > abs(input)) input else ret
    }

    override fun reset() {
        axisStats.clear()
        timeBase = 0

        // Needs to be zero-filled to start.
        while (axisStats.count < HISTORY_SIZE) {
            axisStats.add((timeBase++).toDouble(), 0.0, 0.0)
        }
    }

    companion object {

        const val HISTORY_SIZE = 10
        const val DEFAULT_MIN_MOVE = 0.2
        const val DEFAULT_SLOPE_WEIGHT = 5.0
    }
}
