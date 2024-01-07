package nebulosa.time

import nebulosa.erfa.eraTcbTdb

class TCB : TimeJD, Timescale {

    constructor(jd: DoubleArray, normalize: Boolean = false) : super(jd, normalize)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tcb)

    override fun plus(days: Double) = TCB(whole + days, fraction)

    override fun plus(delta: TimeDelta) = TCB(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = TCB(whole - days, fraction)

    override fun minus(delta: TimeDelta) = TCB(whole, fraction - delta.delta(this))

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai get() = tt.tai

    override val tt get() = tdb.tt

    override val tcg get() = tt.tcg

    override val tdb by lazy { TDB(eraTcbTdb(whole, fraction), true) }

    override val tcb get() = this
}
