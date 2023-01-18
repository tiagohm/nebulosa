package nebulosa.time

import nebulosa.math.twoProduct
import nebulosa.math.twoSum
import kotlin.math.round

open class TimeJD(
    whole: Double,
    fraction: Double = 0.0,
) : InstantOfTime() {

    override val whole: Double

    override val fraction: Double

    constructor(date: DoubleArray) : this(date[0], date[1])

    constructor(time: InstantOfTime) : this(time.whole, time.fraction)

    init {
        with(normalize(whole, fraction)) {
            this@TimeJD.whole = this[0]
            this@TimeJD.fraction = this[1]
        }
    }

    override fun plus(days: Double) = TimeJD(whole + days, fraction)

    override fun minus(days: Double) = TimeJD(whole - days, fraction)

    override val ut1 by lazy { UT1(this) }

    override val utc by lazy { UTC(this) }

    override val tai by lazy { TAI(this) }

    override val tt by lazy { TT(this) }

    override val tcg by lazy { TCG(this) }

    override val tdb by lazy { TDB(this) }

    override val tcb by lazy { TCB(this) }

    companion object {

        inline val Number.jd get() = TimeJD(toDouble())

        @JvmStatic val J2000 = TimeJD(nebulosa.constants.J2000)

        @JvmStatic val B1950 = TimeJD(nebulosa.constants.B1950)

        @JvmStatic
        fun now(): TimeJD = TimeUnix(System.currentTimeMillis() / 1000.0)

        /**
         * Returns the sum of [whole] and [fraction] as two 64-bit floats.
         *
         * The arithmetic is all done with exact floating point operations so no
         * precision is lost to rounding error. It is assumed the sum is less
         * than about 1E16, otherwise the remainder will be greater than 1.0.
         *
         * @return an integer part and the fractional remainder,
         * with the latter guaranteed to be within -0.5 and 0.5 (inclusive on
         * either side, as the integer is rounded to even).
         */
        @JvmStatic
        fun normalize(
            whole: Double,
            fraction: Double = 0.0,
            divisor: Double = Double.NaN,
        ): DoubleArray {
            var (sum, err) = twoSum(whole, fraction)

            if (!divisor.isNaN()) {
                val q1 = sum / divisor
                val (p1, p2) = twoProduct(q1, divisor)
                var (d1, d2) = twoSum(sum, -p1)
                d2 += err
                d2 -= p2
                val q2 = (d1 + d2) / divisor // 3-part float fine here; nothing can be lost.
                twoSum(q1, q2).also { sum = it[0]; err = it[1] }
            }

            var day = round(sum)
            var (extra, frac) = twoSum(sum, -day)
            frac += extra + err

            // Our fraction can now have gotten >0.5 or <-0.5, which means we would
            // loose one bit of precision. So, correct for that.
            day += round(frac)
            var (extra1, frac1) = twoSum(sum, -day)
            frac1 += extra1 + err

            return doubleArrayOf(day, frac1)
        }
    }
}
