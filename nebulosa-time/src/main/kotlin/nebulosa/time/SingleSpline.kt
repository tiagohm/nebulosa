package nebulosa.time

import nebulosa.math.interpolation
import kotlin.math.max

class SingleSpline(private val data: DoubleArray) : Spline<Double> {

    constructor(
        x0: Double,
        y0: Double,
        slope0: Double,
        x1: Double,
        y1: Double,
        slope1: Double,
    ) : this(makeSplineGivenEnds(x0, y0, slope0, x1, y1, slope1))

    override val lower
        get() = data[0]

    override val upper
        get() = data[1]

    override val width
        get() = upper - lower

    override fun get(index: Int) = data[index]

    override fun compute(value: Double): Double {
        val i = interpolation(value, doubleArrayOf(lower), N).toInt()
        val t = (value - lower) / width
        var res = data[2 + i]
        for (k in 3 + i until data.size) res = res * t + data[k]
        return res
    }

    override val derivative: SingleSpline
        get() {
            val columns = DoubleArray(max(3, data.size - 1))

            columns[0] = data[0]
            columns[1] = data[1]

            val width = this.width
            val length = data.size - 1

            for (i in 2 until length) columns[i] = (length - i) * data[i] / width

            return SingleSpline(columns)
        }

    companion object {

        @JvmStatic
        private val N = doubleArrayOf(0.0)

        @JvmStatic
        fun makeSplineGivenEnds(
            x0: Double,
            y0: Double,
            slope0: Double,
            x1: Double,
            y1: Double,
            slope1: Double,
        ): DoubleArray {
            val width = x1 - x0
            val s0 = slope0 * width
            val s1 = slope1 * width
            val a2 = -2.0 * s0 - s1 - 3.0 * y0 + 3.0 * y1
            val a3 = s0 + s1 + 2.0 * y0 - 2.0 * y1
            return doubleArrayOf(x0, x1, a3, a2, s0, y0)
        }
    }
}
