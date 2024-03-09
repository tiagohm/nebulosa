package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.curvePoints

fun interface CurveFitting<T : FittedCurve> {

    fun calculate(points: Collection<CurvePoint>): T

    fun calculate(vararg points: Double) = calculate(points.curvePoints())
}
