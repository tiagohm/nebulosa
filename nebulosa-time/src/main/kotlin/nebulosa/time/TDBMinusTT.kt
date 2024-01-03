package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.constants.TTMINUSTAI
import nebulosa.erfa.eraDtDb

object TDBMinusTT : TimeDelta {

    /**
     * Computes TDB - TT in seconds at [time].
     */
    override fun delta(time: InstantOfTime): Double {
        require(time is TDB || time is TT) { "invalid timescale: $time" }
        // First go from the current input time (which is either
        // TDB or TT) to an approximate UT1. Since TT and TDB are
        // pretty close (few msec?), assume TT. Similarly, since the
        // UT1 terms are very small, use UTC instead of UT1.
        // subtract 0.5, so UT is fraction of the day from midnight.
        val ut = TimeJD.normalize(time.whole - 0.5, time.fraction - TTMINUSTAI / DAYSEC)[1]

        // TODO:
        //        return if (time.location != null) {
        //            val (x, y, z) = time.location
        //            val rxy = hypot(x, y) / 1000.0
        //            val elong = if (location is GeodeticLocation) location.longitude else 0.0
        //            eraDtDb(whole, fraction, ut, elong, rxy, z / 1000.0)
        //        } else {
        //            eraDtDb(time.whole, time.fraction, ut)
        //        }

        return eraDtDb(time.whole, time.fraction, ut)
    }
}
