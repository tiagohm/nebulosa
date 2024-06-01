package nebulosa.curve.fitting

import org.apache.commons.math3.analysis.UnivariateFunction

interface PolynomialCurve : Curve {

    val polynomial: UnivariateFunction

    override fun value(x: Double) = polynomial.value(x)
}
