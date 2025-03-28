package nebulosa.autofocus

import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting

interface AutoFocusListener {

    fun onStarDetected(count: Int, hfd: Double, stdDev: Double, afterExposures: Boolean)

    fun onCurveFitted(
        predictedFocusPoint: CurvePoint?, minX: Double, minY: Double, maxX: Double, maxY: Double,
        trendLine: TrendLineFitting.Curve?, parabolic: QuadraticFitting.Curve?, hyperbolic: HyperbolicFitting.Curve?,
    )
}
