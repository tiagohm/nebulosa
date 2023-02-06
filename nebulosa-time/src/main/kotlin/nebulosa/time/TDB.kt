package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.erfa.eraTdbTcb

class TDB : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tdb)

    override fun plus(days: Double) = TDB(whole + days, fraction)

    override fun minus(days: Double) = TDB(whole - days, fraction)

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai get() = tt.tai

    override val tt by lazy { TT(whole, fraction - TDBMinusTT.delta(this) / DAYSEC) }

    override val tcg get() = tt.tcg

    override val tdb get() = this

    override val tcb by lazy { TCB(eraTdbTcb(whole, fraction)) }
}
