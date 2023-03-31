package nebulosa.guiding.internal

import nebulosa.guiding.GuideAxis
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.sign

data class ResistSwitchGuideAlgorithm(
    override val axis: GuideAxis,
    override var minMove: Double = DEFAULT_MIN_MOVE,
    var aggression: Double = DEFAULT_AGGRESSION, // [0..1]
    var fastSwitchForLargeDeflections: Boolean = true,
) : GuideAlgorithm {

    private var currentSide = 0
    private val history = DoubleArray(HISTORY_SIZE)

    override fun compute(input: Double): Double {
        for (i in 1 until history.size) history[i - 1] = history[i]
        history[HISTORY_SIZE - 1] = input

        if (abs(input) < minMove) return 0.0

        val side = input.sign.toInt()

        if (fastSwitchForLargeDeflections) {
            val thresh = 3.0 * minMove

            if (side != currentSide && abs(input) > thresh) {
                LOG.info("large excursion. input={}, thresh={}, direction={} to {}", input, thresh, currentSide, side)

                currentSide = 0

                history.fill(0.0, 0, HISTORY_SIZE - 3)
                history.fill(input, HISTORY_SIZE - 3, HISTORY_SIZE)
            }
        }

        var decHistory = 0

        for (i in history.indices) {
            if (abs(history[i]) > minMove) {
                decHistory += history[i].sign.toInt()
            }
        }

        if (currentSide == 0 || currentSide == -decHistory) {
            if (abs(decHistory) < 3) {
                LOG.warn("not compelling enough")
                return 0.0
            }

            var oldest = 0.0
            var newest = 0.0

            for (i in 0..2) {
                oldest += history[i]
                newest += history[history.size - (i + 1)]
            }

            if (abs(newest) <= abs(oldest)) {
                LOG.warn("Not getting worse")
                return 0.0
            }

            LOG.info("switching direction from {} to {}", currentSide, decHistory)

            currentSide = decHistory
        }

        if (currentSide != side) {
            LOG.warn("must have overshot -- vetoing move")
            return 0.0
        }

        val result = input * aggression

        LOG.info("result={}, input={}", result, input)

        return result
    }

    override fun reset() {
        currentSide = 0
        history.fill(0.0)
    }

    companion object {

        private const val HISTORY_SIZE = 10
        const val DEFAULT_MIN_MOVE = 0.2
        const val DEFAULT_AGGRESSION = 1.0

        @JvmStatic private val LOG = LoggerFactory.getLogger(ResistSwitchGuideAlgorithm::class.java)
    }
}
