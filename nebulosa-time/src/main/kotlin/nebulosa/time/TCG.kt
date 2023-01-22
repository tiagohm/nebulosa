package nebulosa.time

import nebulosa.erfa.eraTcgTt

class TCG(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = TCG(time + days)

    override fun minus(days: Double) = TCG(time - days)

    override val ut1 by lazy { utc.ut1 }

    override val utc by lazy { tai.utc }

    override val tai by lazy { tt.tai }

    override val tt by lazy { TT(TimeJD(eraTcgTt(whole, fraction))) }

    override val tcg get() = this

    override val tdb by lazy { tt.tdb }

    override val tcb by lazy { tdb.tcb }
}
