package nebulosa.time

import nebulosa.erfa.eraUtcTai
import nebulosa.erfa.eraUtcUt1

class UTC : TimeJD, Timescale {

    constructor(jd: DoubleArray, normalize: Boolean = false) : super(jd, normalize)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.utc)

    override fun plus(days: Double) = UTC(whole + days, fraction)

    override fun plus(delta: TimeDelta) = UTC(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = UTC(whole - days, fraction)

    override fun minus(delta: TimeDelta) = UTC(whole, fraction - delta.delta(this))

    override val ut1 by lazy { UT1(eraUtcUt1(whole, fraction, IERS.delta(this)), true) }

    override val utc get() = this

    override val tai by lazy { TAI(eraUtcTai(whole, fraction), true) }

    override val tt get() = tai.tt

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb

    companion object {

        @JvmStatic
        fun now() = UTC(TimeJD.now())
    }
}
