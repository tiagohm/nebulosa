package nebulosa.time

import nebulosa.constants.DAYSEC

/**
 * Chandra X-ray Center [seconds] from 1998-01-01 00:00:00 TT.
 *
 * For example, 63072064.184 is midnight on January 1, 2000.
 *
 * Must be used with [TT] date.
 */
class TimeCxcSec(val seconds: Double) : TimeFromEpoch(seconds, DAYSEC, 2450814.0, 0.5)
