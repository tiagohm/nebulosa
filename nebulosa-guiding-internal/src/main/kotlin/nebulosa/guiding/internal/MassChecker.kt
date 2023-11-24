package nebulosa.guiding.internal

import java.util.*

internal class MassChecker {

    private data class Entry(
        val time: Long,
        val mass: Float,
    )

    data class CheckedMass(
        @JvmField val limit0: Float = 0f,
        @JvmField val limit1: Float = 0f,
        @JvmField val limit2: Float = 0f,
        @JvmField val limit3: Float = 0f,
        @JvmField val reject: Boolean = true,
    ) {

        companion object {

            @JvmStatic val EMPTY = CheckedMass()
        }
    }

    private val data = LinkedList<Entry>()
    private var highMass = 0f
    private var lowMass = Float.MAX_VALUE
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

    fun add(mass: Float) {
        val now = System.currentTimeMillis()
        val oldest = now - timeWindow

        while (data.isNotEmpty() && data.peek().time < oldest) {
            data.pop()
        }

        val entry = Entry(now, mass)
        // TODO: Verificar se o binarySearch está buscando corretamente!
        val index = data.binarySearch { it.mass.compareTo(mass) }
        if (index >= 0) data.add(index, entry)
        else data.add(-index - 1, entry)
    }

    fun checkMass(mass: Float, threshold: Float = 0.5f): CheckedMass {
        if (data.size < 5) return CheckedMass.EMPTY

        val median = data[data.size / 2]

        if (median.mass > highMass) highMass = median.mass
        if (median.mass < lowMass) lowMass = median.mass

        // Let the low water mark drift to follow the median
        // so that it moves back up after a period of intermittent
        // clouds has brought it down.
        lowMass += 0.05f * (median.mass - lowMass)

        val limit0 = lowMass * (1f - threshold)
        val limit1 = median.mass
        val limit2 = highMass * (1f + threshold)
        // When mass is depressed by sky conditions, we still want to trigger a rejection when
        // there is a large spike in mass, even if it is still below the high water mark-based
        // threhold.
        val limit3 = median.mass * (1f + 2f * threshold)

        val reject = mass < limit0 || mass > limit2 || mass > limit3

        return CheckedMass(limit0, limit1, limit2, limit3, reject)
    }

    fun reset() {
        data.clear()
        highMass = 0f
        lowMass = Float.MAX_VALUE
    }

    companion object {

        const val DEFAULT_TIME_WINDOW = 22500L
    }
}
