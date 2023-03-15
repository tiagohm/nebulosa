package nebulosa.time

import nebulosa.constants.DAYSPERJY

/**
 * Julian epoch year as floating point value like 2000.0.
 */
class TimeJulianEpoch(override val epoch: Double) : TimeJD(2451545.0 + (epoch - 2000.0) * DAYSPERJY), TimeEpochDate {

    override fun toString() = "TimeJulianEpoch(epoch=$epoch, whole=$whole, fraction=$fraction)"
}
