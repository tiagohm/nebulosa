package nebulosa.time

import nebulosa.constants.DTY
import nebulosa.constants.MJD0

/**
 * Besselian epoch year as floating point value like 1950.0.
 */
class TimeBesselianEpoch(override val epoch: Double) : TimeJD(MJD0 + 15019.81352 + (epoch - 1900.0) * DTY), TimeEpochDate {

    companion object {

        @JvmStatic val B1875 = TimeBesselianEpoch(1875.0)
        @JvmStatic val B9150 = TimeBesselianEpoch(1950.0)
    }
}
