package nebulosa.time

import nebulosa.constants.DAYSEC

class UT1(
    val time: InstantOfTime,
) : InstantOfTime() {

    constructor(whole: Double, fraction: Double = 0.0) : this(TimeJD(whole, fraction))

    override val whole get() = time.whole

    override val fraction get() = time.fraction

    override fun plus(days: Double) = UT1(time + days)

    override fun minus(days: Double) = UT1(time - days)

    override val ut1 get() = this

    override val utc by lazy { UTC(TimeJD(whole, fraction - IERS.delta(time) / DAYSEC)) }

    override val tai by lazy { utc.tai }

    override val tt by lazy { tai.tt }

    override val tcg by lazy { tt.tcg }

    override val tdb by lazy { tt.tdb }

    override val tcb by lazy { tdb.tcb }
}
