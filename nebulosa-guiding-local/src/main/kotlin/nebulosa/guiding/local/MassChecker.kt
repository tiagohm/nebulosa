package nebulosa.guiding.local

import java.util.*

class MassChecker {

    private data class Entry(
        val time: Long,
        val mass: Double,
    )

    @Suppress("ArrayInDataClass")
    data class CheckedMass(
        val limits: DoubleArray,
        val reject: Boolean,
    ) {

        companion object {

            @JvmStatic val EMPTY = CheckedMass(DoubleArray(0), true)
        }
    }

    private val data = LinkedList<Entry>()
    private var highMass = 0.0
    private var lowMass = Double.MAX_VALUE
    private var exposure = 0
    private var autoExposure = false
    private var timeWindow = DEFAULT_TIME_WINDOW * 2L

    fun timeWindow(ms: Long) {
        // An abrupt change in mass will affect the median after approx timeWindow / 2.
        timeWindow = ms * 2L
    }

    fun exposure(exposure: Int, autoExposure: Boolean) {
        if (autoExposure != this.autoExposure) {
            this.autoExposure = autoExposure
            this.exposure = exposure
            reset()
        } else if (exposure != this.exposure) {
            this.exposure = exposure

            if (!this.autoExposure) {
                reset()
            }
        }
    }

    fun adjustedMass(mass: Double): Double {
        return if (autoExposure) mass / exposure else mass
    }

    fun add(mass: Double) {
        val now = System.currentTimeMillis()
        val oldest = now - timeWindow

        while (data.isNotEmpty() && data.peek().time < oldest) {
            data.pop()
        }

        data.add(Entry(now, adjustedMass(mass)))
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

        val limits = DoubleArray(4)
        limits[0] = lowMass * (1.0 - threshold)
        limits[1] = median.mass
        limits[2] = highMass * (1.0 + threshold)
        // When mass is depressed by sky conditions, we still want to trigger a rejection when
        // there is a large spike in mass, even if it is still below the high water mark-based
        // threhold.
        limits[3] = median.mass * (1.0 + 2.0 * threshold)

        val adjustedMass = adjustedMass(mass)
        val reject = adjustedMass < limits[0] || adjustedMass > limits[2] || adjustedMass > limits[3]

        if (reject && autoExposure) {
            // Convert back to mass-like numbers for logging by caller.
            for (i in 0..3) limits[i] = limits[i] * exposure
        }

        return CheckedMass(limits, reject)
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
