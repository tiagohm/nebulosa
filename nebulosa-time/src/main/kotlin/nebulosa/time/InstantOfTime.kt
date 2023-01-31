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
 */
abstract class InstantOfTime {

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
    val value get() = whole + fraction

    abstract operator fun plus(days: Double): InstantOfTime

    abstract operator fun minus(days: Double): InstantOfTime

    abstract val ut1: UT1

    abstract val utc: UTC

    abstract val tai: TAI

    abstract val tt: TT

    abstract val tcg: TCG

    abstract val tdb: TDB

    abstract val tcb: TCB

    fun asYearMonthDayAndFraction(cutoff: JulianCalendarCutOff = JulianCalendarCutOff.NONE): DoubleArray {
        val a = whole.toInt()
        var f = a + 1401
        if (a >= cutoff.value) f += ((4 * a + 274277) / 146097 * 3 / 4 - 38)
        val e = 4 * f + 3
        val g = e % 1461 / 4
        val h = 5 * g + 2

        val day = h % 153 / 5 + 1
        val month = (h / 153 + 2) % 12 + 1
        val year = e / 1461 - 4716 + (12 + 2 - month) / 12

        return doubleArrayOf(year.toDouble(), month.toDouble(), day.toDouble(), fraction + 0.5)
    }

    fun asDateTime(cutoff: JulianCalendarCutOff = JulianCalendarCutOff.NONE): LocalDateTime {
        val (year, month, day, fraction) = asYearMonthDayAndFraction(cutoff)

        val date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())!!

        val (i, j) = (fraction * DAYSEC) divmod 3600.0
        val hour = i.toInt()

        val (k, m) = j divmod 60.0
        val minute = k.toInt()

        val (n, o) = m divmod 1.0
        val second = n.toInt()
        val nanoOfSecond = (o * 1E+9).toInt()

        val time = LocalTime.of(hour, minute, second, nanoOfSecond)

        return LocalDateTime.of(date, time)
    }

    /**
     * Returns 3×3 rotation matrix: ICRS -> equinox of this date.
     */
    val m by lazy { eraPnm06a(tt.whole, tt.fraction) }

    /**
     * Returns the nutation angles for this date.
     */
    val nutationAngles by lazy { eraNut06a(tt.whole, tt.fraction) }

    /**
     * Returns the 3×3 precession matrix P for this date.
     */
    val precessionMatrix by lazy { eraPmat06(tt.whole, tt.fraction) }

    /**
     * Returns the 3×3 nutation matrix N for this date.
     */
    val nutationMatrix by lazy { eraNum06a(tt.whole, tt.fraction) }

    val polarMotionMatrix by lazy { IERS.pmMatrix(this) }

    /**
     * Returns Greenwich Apparent Sidereal Time (GAST).
     */
    val gast by lazy { eraGst06a(ut1.whole, ut1.fraction, tt.whole, tt.fraction) }

    /**
     * Returns Greenwich Mean Sidereal Time (GMST).
     */
    val gmst by lazy { eraGmst06(ut1.whole, ut1.fraction, tt.whole, tt.fraction) }

    /**
     * Returns Earth rotation angle (IAU 2000 model).
     */
    val era by lazy { eraEra00(ut1.whole, ut1.fraction) }

    /**
     * Returns the 3x3 matrix of Equation of Origins in cycles.
     */
    val c by lazy { Matrix3D.IDENTITY.rotateZ(eraEra00(ut1.whole, ut1.fraction) - gast) * m }

    /**
     * Returns the true obliquity of the ecliptic in radians.
     */
    val trueObliquity get() = meanObliquity + nutationAngles.second

    /**
     * Returns the mean obliquity of the ecliptic in radians.
     */
    val meanObliquity by lazy { eraObl06(tt.whole, tt.fraction) }
}
