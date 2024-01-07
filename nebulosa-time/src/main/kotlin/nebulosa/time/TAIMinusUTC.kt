package nebulosa.time

import nebulosa.erfa.eraDat
import nebulosa.erfa.eraJd2Cal

object TAIMinusUTC : TimeDelta {

    override fun delta(time: InstantOfTime): Double {
        return eraDat(eraJd2Cal(time.whole, time.fraction))
    }
}
