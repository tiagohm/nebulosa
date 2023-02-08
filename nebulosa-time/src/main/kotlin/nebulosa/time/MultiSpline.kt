package nebulosa.time

import nebulosa.math.interpolation

class MultiSpline(private val data: List<DoubleArray>) : Spline<DoubleArray> {

    override val lower
        get() = data[0]

    override val upper
        get() = data[1]

    override val width by lazy { DoubleArray(upper.size) { upper[it] - lower[it] } }

    override fun get(index: Int) = data[index]

    override fun compute(value: Double): Double {
        val i = interpolation(value, lower).toInt()
        val t = (value - lower[i]) / width[i]
        var res = data[2][i]
        for (k in 3 until data.size) res = (res * t) + data[k][i]
        return res
    }

    override val derivative: MultiSpline
        get() {
            val length = data.size - 1

            val columns = ArrayList<DoubleArray>(length)

            columns.add(lower)
            columns.add(upper)

            for (i in 2 until length) {
                val c = DoubleArray(data[i].size)
                val j = length - i
                for (k in c.indices) c[k] = j * data[i][k] / (upper[k] - lower[k])
                columns.add(c)
            }

            return MultiSpline(columns)
        }
}
