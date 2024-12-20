package nebulosa.time

import nebulosa.erfa.eraTcgTt

class TCG : TimeJD, Timescale {

    constructor(jd: DoubleArray, normalize: Boolean = false) : super(jd, normalize)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tcg)

    override fun plus(days: Double) = TCG(whole + days, fraction)

    override fun plus(delta: TimeDelta) = TCG(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = TCG(whole - days, fraction)

    override fun minus(delta: TimeDelta) = TCG(whole, fraction - delta.delta(this))

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai get() = tt.tai

    override val tt by lazy { TT(eraTcgTt(whole, fraction), true) }

    override val tcg get() = this

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb
}
