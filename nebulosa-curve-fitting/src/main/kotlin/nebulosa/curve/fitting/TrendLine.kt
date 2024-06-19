package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.curvePoints
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import kotlin.math.max
import kotlin.math.sqrt

data class TrendLine(val points: List<CurvePoint>) : LinearCurve {

    constructor() : this(emptyList())

    constructor(vararg points: Double) : this(points.curvePoints())

    constructor(vararg points: CurvePoint) : this(points.toList())

    private val regression = OLSMultipleLinearRegression()

    override val slope: Double
    override val intercept: Double
    override val rSquared: Double

    init {
        val expectedSize = max(3, points.size)
        val y = ArrayList<Double>(expectedSize)
        val x = ArrayList<DoubleArray>(expectedSize)

        for (point in points) {
            val weight = sqrt(point.weight)
            y.add(weight * point.y)
            x.add(doubleArrayOf(weight * point.x, weight))
        }

        if (points.size == 2) {
            y.add(1, (points[0].y + points[1].y) / 2.0)
            x.add(1, doubleArrayOf((points[0].x + points[1].x) / 2.0, 1.0))
        }

        if (points.size >= 2) {
            regression.isNoIntercept = true
            regression.newSampleData(y.toDoubleArray(), x.toTypedArray())

            val regressionParameters = regression.estimateRegressionParameters()

            slope = regressionParameters[0].let { if (it.isFinite()) it else 0.0 }
            intercept = regressionParameters[1].let { if (it.isFinite()) it else 0.0 }
            rSquared = regression.calculateRSquared().let { if (it.isFinite()) it else 0.0 }
        } else {
            slope = 0.0
            intercept = 0.0
            rSquared = 0.0
        }
    }

    override fun value(x: Double) = slope * x + intercept

    companion object {

        @JvmStatic val ZERO = TrendLine()
    }
}
