package nebulosa.curve.fitting

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.fitting.PolynomialCurveFitter

data object QuadraticFitting : CurveFitting<QuadraticFitting.Curve> {

    data class Curve(
        override val polynomial: UnivariateFunction,
        override val minimum: CurvePoint,
        override val rSquared: Double,
    ) : FittedCurve, PolynomialCurve

    override fun calculate(points: Collection<CurvePoint>) = with(PolynomialFunction(FITTER.fit(points))) {
        val rSquared = RSquared.calculate(points, this)
        val minimumX = coefficients[1] / (-2.0 * coefficients[2])
        val minimumY = value(minimumX)
        val minimum = CurvePoint(minimumX, minimumY)
        Curve(this, minimum, rSquared)
    }

    @JvmStatic private val FITTER = PolynomialCurveFitter.create(2)
}
