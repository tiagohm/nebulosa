package nebulosa.time

import nebulosa.erfa.eraTaiTt
import nebulosa.erfa.eraTaiUtc

class TAI : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tai)

    override fun plus(days: Double) = TAI(whole + days, fraction)

    override fun minus(days: Double) = TAI(whole - days, fraction)

    override val ut1 get() = utc.ut1

    override val utc by lazy { UTC(eraTaiUtc(whole, fraction)) }

    override val tai get() = this

    override val tt by lazy { TT(eraTaiTt(whole, fraction)) }

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb
}
