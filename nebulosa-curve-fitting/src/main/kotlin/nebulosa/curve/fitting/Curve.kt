package nebulosa.curve.fitting

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction

fun interface Curve : UnivariateFunction {

    operator fun invoke(x: Double) = value(x)

    companion object {

        internal fun DoubleArray.curvePoints(): Collection<CurvePoint> {
            val points = ArrayList<CurvePoint>(size / 2)

            for (i in indices step 2) {
                points.add(CurvePoint(this[i], this[i + 1]))
            }

            return points
        }

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun DoubleArray.polynomial() = PolynomialFunction(this)
    }
}
