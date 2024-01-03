package nebulosa.time

import nebulosa.erfa.eraUt1Utc

class UT1 : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.ut1)

    override fun plus(days: Double) = UT1(whole + days, fraction)

    override fun minus(days: Double) = UT1(whole - days, fraction)

    override val ut1 get() = this

    override val utc by lazy { UTC(eraUt1Utc(whole, fraction, IERS.delta(this))) }

    override val tai get() = utc.tai

    override val tt get() = tai.tt

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb
}
