package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.curvePoints
import org.apache.commons.math3.stat.regression.SimpleRegression

data class TrendLine(val points: Collection<CurvePoint>) : LinearCurve {

    constructor(vararg points: Double) : this(points.curvePoints())

    private val regression = SimpleRegression()

    init {
        points.forEach { regression.addData(it.x, it.y) }
    }

    override val slope = regression.slope.zeroIfNaN()

    override val intercept = regression.intercept.zeroIfNaN()

    override val rSquared = regression.rSquare.zeroIfNaN()

    override fun value(x: Double) = regression.predict(x)

    companion object {

        @JvmStatic val ZERO = TrendLine()

        @Suppress("NOTHING_TO_INLINE")
        private inline fun Double.zeroIfNaN() = if (isNaN()) 0.0 else this
    }
}
