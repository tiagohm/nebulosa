package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.constants.TTMINUSTAI
import nebulosa.erfa.eraTtTcg

// TODO: Store/pass as nullable parameters TAI, TCG & TDB for conversion to TT.
class TT(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = TT(time + days)

    override fun minus(days: Double) = TT(time - days)

    override val ut1 by lazy { utc.ut1 }

    override val utc by lazy { tai.utc }

    override val tai by lazy { TAI(TimeJD(whole, fraction - TTMINUSTAI / DAYSEC)) }

    override val tt get() = this

    override val tcg by lazy { TCG(TimeJD(eraTtTcg(whole, fraction))) }

    override val tdb by lazy { TDB(TimeJD(whole, fraction + TDBMinusTT.delta(this) / DAYSEC)) }

    override val tcb by lazy { tdb.tcb }
}
