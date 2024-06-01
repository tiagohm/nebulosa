package nebulosa.curve.fitting

interface FittedCurve : Curve {

    val minimum: CurvePoint

    val rSquared: Double
}
