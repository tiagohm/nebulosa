package nebulosa.time

import nebulosa.erfa.eraTtTai
import nebulosa.erfa.eraTtTcg
import nebulosa.erfa.eraTtTdb

class TT : TimeJD, Timescale {

    constructor(jd: DoubleArray, normalize: Boolean = false) : super(jd, normalize)

    constructor(whole: Double, fraction: Double = 0.0) : super(whole, fraction)

    constructor(time: Timescale) : super(time.tt)

    override fun plus(days: Double) = TT(whole + days, fraction)

    override fun plus(delta: TimeDelta) = TT(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = TT(whole - days, fraction)

    override fun minus(delta: TimeDelta) = TT(whole, fraction - delta.delta(this))

    override val ut1 get() = utc.ut1

    override val utc get() = tai.utc

    override val tai by lazy { TAI(eraTtTai(whole, fraction), true) }

    override val tt get() = this

    override val tcg by lazy { TCG(eraTtTcg(whole, fraction), true) }

    override val tdb by lazy { TDB(eraTtTdb(whole, fraction, TDBMinusTT.delta(this)), true) }

    override val tcb get() = tdb.tcb

    companion object {

        @JvmStatic val J2000 = TT(TimeJD.J2000)

        @JvmStatic val B1950 = TT(TimeJD.B1950)

        @JvmStatic
        fun now() = TT(TimeJD.now())
    }
}
