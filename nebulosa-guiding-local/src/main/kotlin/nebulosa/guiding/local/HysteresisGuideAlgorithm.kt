package nebulosa.guiding.local

import kotlin.math.abs

class HysteresisGuideAlgorithm(
    override var minMove: Double = MIN_MOVE,
    val hysteresis: Double = HYSTERESIS,
    val aggression: Double = AGGRESSION,
) : GuideAlgorithm {

    @Volatile var lastMove = 0.0
        private set

    override fun compute(input: Double): Double {
        var res = (1.0 - hysteresis) * input + hysteresis * lastMove

        res *= aggression

        if (abs(input) < minMove) {
            res = 0.0
        }

        lastMove = res

        return res
    }

    override fun reset() {
        lastMove = 0.0
    }

    companion object {

        const val MIN_MOVE = 0.2
        const val HYSTERESIS = 0.1
        const val AGGRESSION = 0.7
        const val MAX_AGGRESSION = 2.0
        const val MAX_HYSTERESIS = 0.99
    }
}
