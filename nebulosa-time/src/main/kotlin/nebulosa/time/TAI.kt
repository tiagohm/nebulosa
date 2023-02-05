package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.constants.TTMINUSTAI

class TAI : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tai)

    override fun plus(days: Double) = TAI(whole + days, fraction)

    override fun minus(days: Double) = TAI(whole - days, fraction)

    override val ut1 get() = utc.ut1

    override val utc get() = UTC(whole, fraction - TAIMinusUTC.delta(this) / DAYSEC)

    override val tai get() = this

    override val tt get() = TT(whole, fraction + TTMINUSTAI / DAYSEC)

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb
}
