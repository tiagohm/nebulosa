package nebulosa.time

import nebulosa.math.twoProduct
import nebulosa.math.twoSum
import kotlin.math.round

open class TimeJD internal constructor(private val jd: DoubleArray, normalize: Boolean = false) : InstantOfTime() {

    final override val whole
        get() = jd[0]

    final override val fraction
        get() = jd[1]

    constructor(whole: Double, fraction: Double = 0.0) : this(doubleArrayOf(whole, fraction), true)

    constructor(time: InstantOfTime) : this(doubleArrayOf(time.whole, time.fraction))

    init {
        if (normalize) {
            normalize(jd[0], jd[1], output = jd)
        }
    }

    override fun plus(days: Double) = TimeJD(whole + days, fraction)

    override fun plus(delta: TimeDelta) = TimeJD(whole, fraction + delta.delta(this))

    override fun minus(days: Double) = TimeJD(whole - days, fraction)

    override fun minus(delta: TimeDelta) = TimeJD(whole, fraction - delta.delta(this))

    override val ut1 get() = UT1(whole, fraction)

    override val utc get() = UTC(whole, fraction)

    override val tai get() = TAI(whole, fraction)

    override val tt get() = TT(whole, fraction)

    override val tcg get() = TCG(whole, fraction)

    override val tdb get() = TDB(whole, fraction)

    override val tcb get() = TCB(whole, fraction)

    companion object {

        @JvmStatic val J2000 = TimeJD(nebulosa.constants.J2000)

        @JvmStatic val B1950 = TimeJD(nebulosa.constants.B1950)

        @JvmStatic
        fun now(): TimeJD = TimeUnix.now()

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
            output: DoubleArray? = null,
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

            return if (output != null) {
                output[0] = day
                output[1] = frac1
                output
            } else {
                doubleArrayOf(day, frac1)
            }
        }
    }
}
