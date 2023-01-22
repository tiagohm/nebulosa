package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.constants.TTMINUSTAI

class TAI(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = TAI(time + days)

    override fun minus(days: Double) = TAI(time - days)

    override val ut1 by lazy { utc.ut1 }

    override val utc by lazy { UTC(TimeJD(whole, fraction - TAIMinusUTC.delta(time) / DAYSEC)) }

    override val tai get() = this

    override val tt by lazy { TT(TimeJD(whole, fraction + TTMINUSTAI / DAYSEC)) }

    override val tcg by lazy { tt.tcg }

    override val tdb by lazy { tt.tdb }

    override val tcb by lazy { tdb.tcb }
}
