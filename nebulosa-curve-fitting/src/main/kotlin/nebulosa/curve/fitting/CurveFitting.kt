package nebulosa.curve.fitting

fun interface CurveFitting {

    fun calculate(points: Collection<CurvePoint>): FittedCurve
}
