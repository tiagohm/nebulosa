package nebulosa.time

import nebulosa.erfa.eraTaiTt
import nebulosa.erfa.eraTaiUtc

class TAI : TimeJD, Timescale {

    constructor(jd: DoubleArray, normalize: Boolean = false) : super(jd, normalize)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tai)

    override fun plus(days: Double) = TAI(whole + days, fraction)

    override fun plus(delta: TimeDelta) = TAI(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = TAI(whole - days, fraction)

    override fun minus(delta: TimeDelta) = TAI(whole, fraction - delta.delta(this))

    override val ut1 get() = utc.ut1

    override val utc by lazy { UTC(eraTaiUtc(whole, fraction), true) }

    override val tai get() = this

    override val tt by lazy { TT(eraTaiTt(whole, fraction), true) }

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb
}
