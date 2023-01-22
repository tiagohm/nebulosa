package nebulosa.time

import nebulosa.constants.DAYSEC

class UTC(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = UTC(time + days)

    override fun minus(days: Double) = UTC(time - days)

    override val ut1 by lazy { UT1(TimeJD(whole, fraction + IERS.delta(time) / DAYSEC)) }

    override val utc get() = this

    override val tai by lazy { TAI(TimeJD(whole, fraction + TAIMinusUTC.delta(time) / DAYSEC)) }

    override val tt by lazy { tai.tt }

    override val tcg by lazy { tt.tcg }

    override val tdb by lazy { tt.tdb }

    override val tcb by lazy { tdb.tcb }
}
