package nebulosa.curve.fitting

data object TrendLineFitting : CurveFitting<TrendLineFitting.Curve> {

    data class Curve(
        val left: TrendLine,
        val right: TrendLine,
        override val minimum: CurvePoint,
    ) : FittedCurve {

        val intersection = left.intersect(right)

        override val rSquared = (left.rSquared + right.rSquared) / 2.0

        override fun value(x: Double) = if (x < minimum.x) left(x)
        else if (x > minimum.x) right(x)
        else minimum.y
    }

    override fun calculate(points: Collection<CurvePoint>): Curve {
        val minimum = points.minBy { it.y }

        val left = TrendLine(points.filter { it.x < minimum.x && it.y > minimum.y + 0.1 })
        val right = TrendLine(points.filter { it.x > minimum.x && it.y > minimum.y + 0.1 })

        return Curve(left, right, minimum)
    }
}
