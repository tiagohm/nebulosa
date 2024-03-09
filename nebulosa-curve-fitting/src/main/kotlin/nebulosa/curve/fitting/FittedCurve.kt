package nebulosa.curve.fitting

interface FittedCurve : Curve {

    val minimum: CurvePoint

    val rSquared: Double

    val minimumX
        get() = minimum.x

    val minimumY
        get() = minimum.y
}
