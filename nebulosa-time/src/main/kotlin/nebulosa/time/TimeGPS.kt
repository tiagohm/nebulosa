package nebulosa.time

import nebulosa.constants.DAYSEC

/**
 * GPS time: [seconds] from 1980-01-06 00:00:00 UTC
 *
 * For example, 630720013.0 is midnight on January 1, 2000.
 *
 * Must be used with [TAI] date.
 *
 * @see <a href="https://www.usno.navy.mil/USNO/time/gps/usno-gps-time-transfer">Details</a>
 */
data class TimeGPS(val seconds: Double) : TimeFromEpoch(seconds, DAYSEC, 2444245.0, -0.4997800925925926)
