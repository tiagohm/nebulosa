package nebulosa.time

import nebulosa.constants.DAYSEC

/**
 * Unix [seconds] (UTC): seconds from 1970-01-01 00:00:00 UTC, ignoring leap seconds.
 *
 * For example, 946684800.0 in Unix time is midnight on January 1, 2000.
 *
 * Must be used with [UTC] date.
 *
 * This quantity is not exactly unix time and differs from the strict POSIX definition
 * by up to 1 second on days with a leap second. POSIX unix time actually jumps backward by 1
 * second at midnight on leap second days while this class value is monotonically increasing
 * at 86400 seconds per UTC day.
 */
data class TimeUnix(val seconds: Double) : TimeFromEpoch(seconds, DAYSEC, 2440588.0, -0.5)
