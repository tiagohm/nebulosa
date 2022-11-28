package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.erfa.eraTdbTcb

class TDB(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = TDB(time + days)

    override fun minus(days: Double) = TDB(time - days)

    override val ut1 by lazy { utc.ut1 }

    override val utc by lazy { tai.utc }

    override val tai by lazy { tt.tai }

    override val tt by lazy { TT(TimeJD(whole, fraction - TDBMinusTT.delta(this) / DAYSEC)) }

    override val tcg by lazy { tt.tcg }

    override val tdb get() = this

    override val tcb by lazy { TCB(TimeJD(eraTdbTcb(whole, fraction))) }
}
