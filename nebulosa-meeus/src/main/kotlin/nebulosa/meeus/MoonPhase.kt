package nebulosa.meeus

import kotlin.math.floor

object MoonPhase {

    const val CK = 1 / 1236.85
    const val SYNODIC_MONTH = 29.530588861

    // (49.1) p. 349
    @JvmStatic
    fun mean(time: Double): Double {
        return Base.horner(time, 2451550.09766, SYNODIC_MONTH / CK, 0.00015437, -0.00000015, 0.00000000073)
    }

    /**
     * Returns k at specified [quarter] nearest [year].
     */
    @JvmStatic
    fun snap(year: Double, quarter: Double): Double {
        val k = (year - 2000.0) * 12.3685 // (49.2) p. 350
        return floor(k - quarter + 0.5) + quarter
    }

    /**
     * Returns the JDE of the mean New Moon nearest the given decimal [year].
     * The mean date is within 0.5 day of the true date of New Moon.
     */
    @JvmStatic
    fun meanNew(year: Double): Double {
        return mean(snap(year, 0.0) * CK)
    }
}
