package nebulosa.guiding.internal

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class WindowedAxisStats(val autoWindowSize: Int = 0) : AxisStats() {

    val autoWindowing = autoWindowSize > 0

    override fun add(deltaTime: Double, starPos: Double, guideAmount: Double) {
        super.add(deltaTime, starPos, guideAmount)

        if (autoWindowing && count > autoWindowSize) {
            removeOldestEntry()
        }
    }

    fun removeOldestEntry() {
        if (count <= 0) return

        val target = guidingEntries.first
        val value = target.starPos
        val deltaTime = target.deltaTime

        sumY -= value
        sumYSq -= value * value
        sumX -= deltaTime
        sumXSq -= deltaTime * deltaTime
        sumXY -= deltaTime * value

        if (target.reversal) axisReversals--
        if (target.guided) axisMoves--

        // Will process list only if required.
        adjustMinMaxValues()

        guidingEntries.pop()

        maxDeltaInx--
    }

    private fun adjustMinMaxValues() {
        val target = guidingEntries.first
        var recalNeeded = false
        var prev = target.starPos

        if (count > 1) {
            // Minimize recalculations.
            recalNeeded = target.starPos == maxDisplacement
                    || target.starPos == minDisplacement
                    || maxDeltaInx == 0

            if (recalNeeded) {
                minDisplacement = Double.MAX_VALUE
                maxDisplacement = Double.MIN_VALUE
                maxDelta = 0.0
            }
        }

        if (recalNeeded) {
            // Dont start at zero, that will be removed.
            val guidingEntriesIter = guidingEntries.listIterator(1)
            var index = 1

            while (guidingEntriesIter.hasNext()) {
                val entry = guidingEntriesIter.next()

                minDisplacement = min(entry.starPos, minDisplacement)
                maxDisplacement = max(entry.starPos, maxDisplacement)

                if (index > 1) {
                    val delta = abs(entry.starPos - prev)

                    if (delta > maxDelta) {
                        maxDelta = delta
                        maxDeltaInx = index
                    }
                }

                prev = entry.starPos
                index++
            }
        }
    }
}
