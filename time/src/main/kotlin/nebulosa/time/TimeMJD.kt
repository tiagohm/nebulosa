package nebulosa.time

import nebulosa.constants.MJD0

/**
 * Modified Julian Date time format.
 *
 * This represents the number of days since midnight on November 17, 1858.
 *
 * For example, 51544.0 in MJD is midnight on January 1, 2000.
 */
data class TimeMJD(val mjd: Double) : TimeJD(normalize(mjd).let { normalize(it[0] + MJD0, it[1]) })
