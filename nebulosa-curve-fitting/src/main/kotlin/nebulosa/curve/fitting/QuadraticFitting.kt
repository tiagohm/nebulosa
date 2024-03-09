package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.polynomial
import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.fitting.PolynomialCurveFitter

data object QuadraticFitting : CurveFitting<QuadraticFitting.Curve> {

    data class Curve(
        private val poly: UnivariateFunction,
        override val minimum: CurvePoint,
        override val rSquared: Double,
    ) : FittedCurve {

        override fun value(x: Double) = poly.value(x)
    }

    override fun calculate(points: Collection<CurvePoint>) = with(FITTER.fit(points).polynomial()) {
        val rSquared = RSquared.calculate(points, this)
        val minimumX = coefficients[1] / (-2.0 * coefficients[2])
        val minimumY = value(minimumX)
        val minimum = CurvePoint(minimumX, minimumY)
        Curve(this, minimum, rSquared)
    }

    @JvmStatic private val FITTER = PolynomialCurveFitter.create(2)
}
