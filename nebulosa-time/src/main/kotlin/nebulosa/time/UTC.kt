package nebulosa.time

import nebulosa.erfa.eraUtcTai
import nebulosa.erfa.eraUtcUt1

class UTC : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.utc)

    override fun plus(days: Double) = UTC(whole + days, fraction)

    override fun minus(days: Double) = UTC(whole - days, fraction)

    override val ut1 by lazy { eraUtcUt1(whole, fraction, IERS.delta(this)).let { UT1(it[0], it[1]) } }

    override val utc get() = this

    override val tai by lazy { eraUtcTai(whole, fraction).let { TAI(it[0], it[1]) } }

    override val tt get() = tai.tt

    override val tcg get() = tt.tcg

    override val tdb get() = tt.tdb

    override val tcb get() = tdb.tcb

    companion object {

        @JvmStatic
        fun now() = UTC(TimeJD.now())
    }
}
