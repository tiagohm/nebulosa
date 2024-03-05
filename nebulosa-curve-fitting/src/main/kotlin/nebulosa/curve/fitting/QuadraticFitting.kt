package nebulosa.curve.fitting

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.fitting.PolynomialCurveFitter

object QuadraticFitting : CurveFitting {

    private val fitter = PolynomialCurveFitter.create(2)

    override fun calculate(points: Collection<CurvePoint>) = object : FittedCurve {

        private val poly by lazy { PolynomialFunction(fitter.fit(points)) }

        override val rSquared by lazy { RSquared.calculate(points, poly) }

        override val minimumX by lazy { poly.coefficients[1] / (-2.0 * poly.coefficients[2]) }

        override val minimumY by lazy { this(minimumX) }

        override fun invoke(x: Double) = poly.value(x)
    }
}
