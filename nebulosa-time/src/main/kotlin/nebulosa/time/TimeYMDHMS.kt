package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.math.divmod
import java.time.LocalDateTime

/**
 * A Time format to represent Time as [year], [month], [day], [hour], [minute] and [second].
 */
class TimeYMDHMS(
    val year: Int,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Double = 0.0,
    val cutoff: JulianCalendarCutOff = JulianCalendarCutOff.NONE,
) : TimeJD(day(year, month, day, cutoff) - 0.5, (second + minute * 60.0 + hour * 3600.0) / DAYSEC) {

    constructor(date: LocalDateTime) : this(date.year, date.monthValue, date.dayOfMonth, date.hour, date.minute, date.second + date.nano / 1E+9)

    override fun toString(): String {
        return "TimeYMDHMS(whole=$whole, fraction=$fraction, year=$year, month=$month, day=$day, hour=$hour, minute=$minute, second=$second, cutoff=$cutoff)"
    }

    companion object {

        /**
         * Computes the Julian day from [year], [month] and [day].
         */
        @JvmStatic
        fun day(
            year: Int,
            month: Int = 1,
            day: Int = 1,
            cutoff: JulianCalendarCutOff = JulianCalendarCutOff.NONE,
        ): Int {
            // Support months <1 and >12 by overflowing cleanly into adjacent years.
            var (y, m) = month - 1 divmod 12
            y += year
            m += 1

            // See the Explanatory Supplement to the Astronomical Almanac 15.11.
            val janfeb = if (m <= 2) 1 else 0
            val g = y + 4716 - janfeb
            val f = (m + 9) % 12
            val e = 1461 * g / 4 + day - 1402
            val j = e + (153 * f + 2) / 5

            val mask = if (j >= cutoff.value) 1 else 0
            return j + (38 - (g + 184) / 100 * 3 / 4) * mask
        }
    }
}
