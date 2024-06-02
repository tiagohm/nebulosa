package nebulosa.curve.fitting

interface LinearCurve : Curve {

    val slope: Double

    val intercept: Double

    val rSquared: Double

    fun intersect(line: TrendLine): CurvePoint {
        if (slope == line.slope) return CurvePoint.ZERO

        val x = (line.intercept - intercept) / (slope - line.slope)
        val y = slope * x + intercept

        return CurvePoint(x, y)
    }
}
