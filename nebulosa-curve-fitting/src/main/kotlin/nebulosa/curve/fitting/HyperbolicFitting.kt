package nebulosa.curve.fitting

import kotlin.math.*

// https://bitbucket.org/Isbeorn/nina/src/master/NINA.Core.WPF/Utility/AutoFocus/HyperbolicFitting.cs

data object HyperbolicFitting : CurveFitting<HyperbolicFitting.Curve> {

    data class Curve(
        @JvmField val a: Double,
        @JvmField val b: Double,
        @JvmField val p: Double,
        override val minimum: CurvePoint,
        @JvmField val points: Collection<CurvePoint>,
    ) : FittedCurve {

        override val rSquared by lazy { RSquared.calculate(points, this) }

        override fun value(x: Double) = a * cosh(asinh((p - x) / b))
    }

    override fun calculate(points: Collection<CurvePoint>): Curve {
        var lowestError = Double.MAX_VALUE

        val nonZeroPoints = points.filter { it.y >= 0.1 }

        if (nonZeroPoints.isEmpty()) {
            throw IllegalArgumentException("No non-zero points in curve. No fit can be calculated.")
        }

        val lowestPoint = nonZeroPoints.minBy { it.y }
        val highestPoint = nonZeroPoints.maxBy { it.y }

        var highestPosition = highestPoint.x
        val highestHfr = highestPoint.y
        val lowestPosition = lowestPoint.x
        val lowestHfr = lowestPoint.y
        var oldError = Double.MAX_VALUE

        // Always go up
        if (highestPosition < lowestPosition) {
            highestPosition = 2 * lowestPosition - highestPosition
        }

        // Get good starting values for a, b and p.
        var a = lowestHfr // a is near the lowest HFR value
        // Alternative hyperbola formula: sqr(y)/sqr(a)-sqr(x)/sqr(b)=1 ==>  sqr(b)=sqr(x)*sqr(a)/(sqr(y)-sqr(a)
        var b = sqrt((highestPosition - lowestPosition) * (highestPosition - lowestPosition) * a * a / (highestHfr * highestHfr - a * a))
        var p = lowestPosition

        var iterationCycles = 0 // How many cycles where used for curve fitting

        var aRange = a
        var bRange = b
        var pRange = highestPosition - lowestPosition // Large steps since slope could contain some error

        if (aRange.isNaN() || bRange.isNaN() || aRange == 0.0 || bRange == 0.0 || pRange == 0.0) {
            throw IllegalArgumentException("Not enough valid data points to fit a curve.")
        }

        do {
            val p0 = p
            val b0 = b
            val a0 = a

            // Reduce range by 50%
            aRange *= 0.5
            bRange *= 0.5
            pRange *= 0.5

            // Start value
            var p1 = p0 - pRange

            // Position loop
            while (p1 <= p0 + pRange) {
                var a1 = a0 - aRange

                while (a1 <= a0 + aRange) {
                    var b1 = b0 - bRange

                    while (b1 <= b0 + bRange) {
                        val error1 = scaledErrorHyperbola(nonZeroPoints, p1, a1, b1)

                        // Better position found
                        if (error1 < lowestError) {
                            oldError = lowestError
                            lowestError = error1

                            // Best value up to now
                            a = a1
                            b = b1
                            p = p1
                        }

                        // do 20 steps within range, many steps guarantees convergence
                        b1 += bRange * 0.1
                    }

                    a1 += aRange * 0.1
                }

                p1 += pRange * 0.1
            }
        } while (oldError - lowestError >= 0.0001 && lowestError > 0.0001 && ++iterationCycles < 30)

        val minimum = CurvePoint(round(p), a)

        return Curve(a, b, p, minimum, nonZeroPoints)
    }

    private fun scaledErrorHyperbola(points: Collection<CurvePoint>, perfectFocusPosition: Double, a: Double, b: Double): Double {
        return sqrt(points.sumOf { (hyperbolicFittingHfrCalc(it.x, perfectFocusPosition, a, b) - it.y).pow(2.0) })
    }

    private fun hyperbolicFittingHfrCalc(position: Double, perfectFocusPosition: Double, a: Double, b: Double): Double {
        val x = perfectFocusPosition - position
        val t = asinh(x / b) // Calculate t-position in hyperbola
        return a * cosh(t) // Convert t-position to y/hfd value
    }
}
