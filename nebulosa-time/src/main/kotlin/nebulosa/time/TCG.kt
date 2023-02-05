package nebulosa.time

import nebulosa.erfa.eraTcgTt

class TCG : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tcg)

    override fun plus(days: Double) = TCG(whole + days, fraction)

    override fun minus(days: Double) = TCG(whole - days, fraction)

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai get() = tt.tai

    override val tt get() = TT(eraTcgTt(whole, fraction))

    override val tcg get() = this

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb
}
