package nebulosa.curve.fitting

import nebulosa.curve.fitting.Curve.Companion.curvePoints
import org.apache.commons.math3.stat.regression.SimpleRegression

data class TrendLine(val points: Collection<CurvePoint>) : LinearCurve {

    constructor(vararg points: Double) : this(points.curvePoints())

    private val regression = SimpleRegression()

    init {
        points.forEach { regression.addData(it.x, it.y) }
    }

    override val slope = regression.slope.let { if (it.isNaN()) 0.0 else it }

    override val intercept = regression.intercept.let { if (it.isNaN()) 0.0 else it }

    override val rSquared = regression.rSquare.let { if (it.isNaN()) 0.0 else it }

    override fun value(x: Double) = if (points.isEmpty()) 0.0 else regression.predict(x)

    companion object {

        @JvmStatic val ZERO = TrendLine()
    }
}
