package nebulosa.time

import nebulosa.erfa.eraTdbTcb
import nebulosa.erfa.eraTdbTt

class TDB : TimeJD, Timescale {

    constructor(jd: DoubleArray, normalize: Boolean = false) : super(jd, normalize)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tdb)

    override fun plus(days: Double) = TDB(whole + days, fraction)

    override fun plus(delta: TimeDelta) = TDB(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = TDB(whole - days, fraction)

    override fun minus(delta: TimeDelta) = TDB(whole, fraction - delta.delta(this))

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai get() = tt.tai

    override val tt by lazy { TT(eraTdbTt(whole, fraction, TDBMinusTT.delta(this)), true) }

    override val tcg get() = tt.tcg

    override val tdb get() = this

    override val tcb by lazy { TCB(eraTdbTcb(whole, fraction), true) }
}
