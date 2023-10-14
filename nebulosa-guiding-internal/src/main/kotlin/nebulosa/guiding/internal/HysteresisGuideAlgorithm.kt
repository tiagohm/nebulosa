package nebulosa.guiding.internal

import kotlin.math.abs

data class HysteresisGuideAlgorithm(
    override val axis: GuideAxis,
    override var minMove: Double = DEFAULT_MIN_MOVE, // [0..20] px
    var hysteresis: Double = DEFAULT_HYSTERESIS, // [0..1]
    var aggression: Double = DEFAULT_AGGRESSION, // [0..2]
) : GuideAlgorithm {

    private var lastMove = 0.0

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

        const val DEFAULT_MIN_MOVE = 0.2
        const val DEFAULT_HYSTERESIS = 0.1
        const val DEFAULT_AGGRESSION = 0.7
        const val MAX_AGGRESSION = 2.0
        const val MAX_HYSTERESIS = 0.99
    }
}
