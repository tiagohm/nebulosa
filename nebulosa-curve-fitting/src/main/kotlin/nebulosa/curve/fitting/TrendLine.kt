package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.curvePoints
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import kotlin.math.max
import kotlin.math.sqrt

data class TrendLine(val points: List<CurvePoint>) : LinearCurve {

    constructor(vararg points: Double) : this(points.curvePoints())

    private val regression = OLSMultipleLinearRegression()
    private val isPointsEnough = points.size > 1

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

        if (isPointsEnough) {
            regression.isNoIntercept = true
            regression.newSampleData(y.toDoubleArray(), x.toTypedArray())
        }
    }

    private val regressionParameters = if (isPointsEnough) regression.estimateRegressionParameters() else doubleArrayOf(0.0, 0.0)

    override val slope = regressionParameters[0].let { if (it.isFinite()) it else 0.0 }
    override val intercept = regressionParameters[1].let { if (it.isFinite()) it else 0.0 }
    override val rSquared = if (isPointsEnough) regression.calculateRSquared().let { if (it.isFinite()) it else 0.0 } else 0.0

    override fun value(x: Double) = if (isPointsEnough) slope * x + intercept else 0.0

    companion object {

        @JvmStatic val ZERO = TrendLine()
    }
}
