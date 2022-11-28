package nebulosa.time

import nebulosa.erfa.eraTcbTdb

class TCB(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = TCB(time + days)

    override fun minus(days: Double) = TCB(time - days)

    override val ut1 by lazy { utc.ut1 }

    override val utc by lazy { tai.utc }

    override val tai by lazy { tt.tai }

    override val tt by lazy { tdb.tt }

    override val tcg by lazy { tt.tcg }

    override val tdb by lazy { TDB(TimeJD(eraTcbTdb(whole, fraction))) }

    override val tcb get() = this
}
