package nebulosa.curve.fitting

import org.apache.commons.math3.analysis.UnivariateFunction

data object ZeroUnivariateFunction : UnivariateFunction {

    override fun value(x: Double) = 0.0
}
