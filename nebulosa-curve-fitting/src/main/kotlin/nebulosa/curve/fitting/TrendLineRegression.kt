package nebulosa.curve.fitting

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.apache.commons.math3.stat.regression.SimpleRegression
import kotlin.math.sqrt

internal sealed interface TrendLineRegression {

    data class RegressionParameters(
        @JvmField val slope: Double,
        @JvmField val intercept: Double,
        @JvmField val rSquared: Double,
    ) {

        companion object {

            @JvmStatic val ZERO = RegressionParameters(0.0, 0.0, 0.0)
        }
    }

    fun compute(points: List<CurvePoint>): RegressionParameters

    data object OLSMultipleLinear : TrendLineRegression {

        override fun compute(points: List<CurvePoint>): RegressionParameters {
            if (points.size <= 2) {
                return RegressionParameters.ZERO
            } else {
                val weights = DoubleArray(points.size) { sqrt(points[it].weight) }
                val x = Array(points.size) { doubleArrayOf(weights[it] * points[it].x, weights[it]) }
                val y = DoubleArray(points.size) { weights[it] * points[it].y }

                val regression = OLSMultipleLinearRegression()
                regression.isNoIntercept = true
                regression.newSampleData(y, x)

                val regressionParameters = regression.estimateRegressionParameters()

                return RegressionParameters(regressionParameters[0], regressionParameters[1], regression.calculateRSquared())
            }
        }
    }

    data object Simple : TrendLineRegression {

        override fun compute(points: List<CurvePoint>): RegressionParameters {
            val regression = SimpleRegression()
            val weights = DoubleArray(points.size) { sqrt(points[it].weight) }
            points.forEachIndexed { i, p -> regression.addData(weights[i] * p.x, weights[i] * p.y) }
            return RegressionParameters(regression.slope, regression.intercept, regression.rSquare)
        }
    }
}
