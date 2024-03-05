package nebulosa.curve.fitting

interface FittedCurve {

    val minimumX: Double

    val minimumY: Double

    val rSquared: Double

    operator fun invoke(x: Double): Double
}
