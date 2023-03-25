package nebulosa.guiding.internal

import java.util.*

internal class MassChecker {

    private data class Entry(
        val time: Long,
        val mass: Double,
    )

    data class CheckedMass(
        val limit0: Double = 0.0,
        val limit1: Double = 0.0,
        val limit2: Double = 0.0,
        val limit3: Double = 0.0,
        val reject: Boolean = true,
    ) {

        companion object {

            @JvmStatic val EMPTY = CheckedMass()
        }
    }

    private val data = LinkedList<Entry>()
    private var highMass = 0.0
    private var lowMass = Double.MAX_VALUE
    private var exposure = 0L
    private var timeWindow = DEFAULT_TIME_WINDOW * 2L

    fun timeWindow(ms: Long) {
        // An abrupt change in mass will affect the median after approx timeWindow / 2.
        timeWindow = ms * 2L
    }

    fun exposure(exposure: Long) {
        if (exposure != this.exposure) {
            this.exposure = exposure
            reset()
        }
    }

    fun add(mass: Double) {
        val now = System.currentTimeMillis()
        val oldest = now - timeWindow

        while (data.isNotEmpty() && data.peek().time < oldest) {
            data.pop()
        }

        data.add(Entry(now, mass))
    }

    fun checkMass(mass: Double, threshold: Double = 0.5): CheckedMass {
        if (data.size < 5) return CheckedMass.EMPTY

        val sortedData = data.sortedBy { it.mass }
        val median = sortedData[sortedData.size / 2]

        if (median.mass > highMass) highMass = median.mass
        if (median.mass < lowMass) lowMass = median.mass

        // Let the low water mark drift to follow the median
        // so that it moves back up after a period of intermittent
        // clouds has brought it down.
        lowMass += 0.05 * (median.mass - lowMass)

        val limit0 = lowMass * (1.0 - threshold)
        val limit1 = median.mass
        val limit2 = highMass * (1.0 + threshold)
        // When mass is depressed by sky conditions, we still want to trigger a rejection when
        // there is a large spike in mass, even if it is still below the high water mark-based
        // threhold.
        val limit3 = median.mass * (1.0 + 2.0 * threshold)

        val reject = mass < limit0 || mass > limit2 || mass > limit3

        return CheckedMass(limit0, limit1, limit2, limit3, reject)
    }

    fun reset() {
        data.clear()
        highMass = 0.0
        lowMass = Double.MAX_VALUE
    }

    companion object {

        const val DEFAULT_TIME_WINDOW = 22500L
    }
}
