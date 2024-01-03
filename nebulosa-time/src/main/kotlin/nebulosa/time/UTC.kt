package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.erfa.eraUtcTai
import nebulosa.erfa.eraUtcUt1

class UTC : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.utc)

    override fun plus(days: Double) = UTC(whole + days, fraction)

    override fun minus(days: Double) = UTC(whole - days, fraction)

    override val ut1 by lazy { UT1(eraUtcUt1(whole, fraction, IERS.delta(this))) }

    override val utc get() = this

    override val tai by lazy { TAI(eraUtcTai(whole, fraction)) }

    override val tt get() = tai.tt

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb

    companion object {

        @JvmStatic
        fun now() = UTC(TimeJD.now())
    }
}
