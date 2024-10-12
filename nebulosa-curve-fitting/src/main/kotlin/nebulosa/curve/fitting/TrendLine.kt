package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.curvePoints
import nebulosa.log.loggerFor
import nebulosa.log.w

data class TrendLine(val points: List<CurvePoint>) : LinearCurve {

    constructor() : this(emptyList())

    constructor(vararg points: Double) : this(points.curvePoints())

    constructor(vararg points: CurvePoint) : this(points.toList())

    override val slope: Double
    override val intercept: Double
    override val rSquared: Double

    init {
        val data = if (points.size <= 1) {
            TrendLineRegression.RegressionParameters.ZERO
        } else if (points.size == 2) {
            TrendLineRegression.Simple.compute(points)
        } else try {
            TrendLineRegression.OLSMultipleLinear.compute(points)
        } catch (e: Throwable) {
            LOG.w("failed to compute regression using OLS Multiple Linear", e.message)
            TrendLineRegression.Simple.compute(points)
        }

        slope = data.slope
        intercept = data.intercept
        rSquared = data.rSquared
    }

    override fun value(x: Double) = slope * x + intercept

    companion object {

        @JvmStatic val ZERO = TrendLine()

        @JvmStatic private val LOG = loggerFor<TrendLine>()
    }
}
