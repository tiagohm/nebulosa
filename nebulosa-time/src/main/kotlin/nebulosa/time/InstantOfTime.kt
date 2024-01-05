package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.erfa.*
import nebulosa.math.Matrix3D
import nebulosa.math.divmod
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Represents an instant of time.
 *
 * All time manipulations and arithmetic operations are done internally using
 * two 64-bit floats to represent time. Floating point algorithms are used
 * so that the Time object maintains sub-nanosecond precision over times spanning
 * the age of the universe.
 *
 * @see <img src="https://docs.astropy.org/en/stable/_images/time_scale_conversion.png"/>
 */
@Suppress("NOTHING_TO_INLINE")
abstract class InstantOfTime : Timescale {

    /**
     * Number of days.
     */
    abstract val whole: Double

    /**
     * Fraction of day.
     */
    abstract val fraction: Double

    /**
     * Gets time representation from internal [whole] and [fraction].
     */
    val value
        get() = whole + fraction

    inline operator fun component1() = whole

    inline operator fun component2() = fraction

    abstract operator fun plus(days: Double): InstantOfTime

    abstract operator fun plus(delta: TimeDelta): InstantOfTime

    abstract operator fun minus(days: Double): InstantOfTime

    abstract operator fun minus(delta: TimeDelta): InstantOfTime

    fun asYearMonthDayAndFraction(cutoff: JulianCalendarCutOff = JulianCalendarCutOff.NONE): Pair<IntArray, DoubleArray> {
        val a = whole.toInt()
        var f = a + 1401
        if (a >= cutoff.value) f += (4 * a + 274277) / 146097 * 3 / 4 - 38
        val e = 4 * f + 3
        val g = e % 1461 / 4
        val h = 5 * g + 2

        val day = h % 153 / 5 + 1
        val month = (h / 153 + 2) % 12 + 1
        val year = e / 1461 - 4716 + (12 + 2 - month) / 12

        return intArrayOf(year, month, day) to doubleArrayOf(fraction + 0.5)
    }

    fun asDateTime(cutoff: JulianCalendarCutOff = JulianCalendarCutOff.NONE): LocalDateTime {
        val (yearMonthDay, fraction) = asYearMonthDayAndFraction(cutoff)
        val (year, month, day) = yearMonthDay

        val (i, j) = (fraction[0] * DAYSEC) divmod 3600.0
        val (extra, hour) = i divmod 24.0

        val date = LocalDate.of(year, month, day).plusDays(extra.toLong())

        val (k, m) = j divmod 60.0
        val minute = k.toInt()

        val (n, o) = m divmod 1.0
        val second = n.toInt()
        val nanoOfSecond = (o * 1E+9).toInt()

        val time = LocalTime.of(hour.toInt(), minute, second, nanoOfSecond)

        return LocalDateTime.of(date, time)
    }

    /**
     * Returns 3×3 rotation matrix: ICRS -> equinox of this date.
     */
    open val m by lazy { eraPnm06a(tt.whole, tt.fraction) }

    /**
     * Returns the nutation angles for this date.
     */
    open val nutationAngles by lazy { eraNut06a(tt.whole, tt.fraction) }

    /**
     * Returns the 3×3 precession matrix P for this date.
     */
    open val precessionMatrix by lazy { eraPmat06(tt.whole, tt.fraction) }

    /**
     * Returns the 3×3 nutation matrix N for this date.
     */
    open val nutationMatrix by lazy { eraNum06a(tt.whole, tt.fraction) }

    open val polarMotionMatrix by lazy { IERS.pmMatrix(this) }

    /**
     * Returns Greenwich Apparent Sidereal Time (GAST).
     */
    open val gast by lazy { eraGst06a(ut1.whole, ut1.fraction, tt.whole, tt.fraction) }

    /**
     * Returns Greenwich Mean Sidereal Time (GMST).
     */
    open val gmst by lazy { eraGmst06(ut1.whole, ut1.fraction, tt.whole, tt.fraction) }

    /**
     * Returns Earth rotation angle (IAU 2000 model).
     */
    open val era by lazy { eraEra00(ut1.whole, ut1.fraction) }

    /**
     * Returns the 3x3 matrix of Equation of Origins in cycles.
     */
    open val c by lazy { Matrix3D.rotZ(eraEra00(ut1.whole, ut1.fraction) - gast) * m }

    /**
     * Returns the true obliquity of the ecliptic in radians.
     */
    open val trueObliquity by lazy { meanObliquity + nutationAngles.second }

    /**
     * Returns the mean obliquity of the ecliptic in radians.
     */
    open val meanObliquity by lazy { eraObl06(tt.whole, tt.fraction) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InstantOfTime) return false

        if (whole != other.whole) return false
        if (fraction != other.fraction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = whole.hashCode()
        result = 31 * result + fraction.hashCode()
        return result
    }

    override fun toString() = "${javaClass.simpleName}(whole=$whole, fraction=$fraction)"
}
