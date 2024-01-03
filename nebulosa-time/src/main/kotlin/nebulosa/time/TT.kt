package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.erfa.eraTtTai
import nebulosa.erfa.eraTtTcg

class TT : TimeJD, Timescale {

    constructor(normalized: DoubleArray) : super(normalized)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tt)

    override fun plus(days: Double) = TT(whole + days, fraction)

    override fun minus(days: Double) = TT(whole - days, fraction)

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai by lazy { eraTtTai(whole, fraction).let { TAI(it[0], it[1]) } }

    override val tt get() = this

    override val tcg by lazy { eraTtTcg(whole, fraction).let { TCG(it[0], it[1]) } }

    override val tdb by lazy { TDB(whole, fraction + TDBMinusTT.delta(this) / DAYSEC) }

    override val tcb get() = tdb.tcb
}
