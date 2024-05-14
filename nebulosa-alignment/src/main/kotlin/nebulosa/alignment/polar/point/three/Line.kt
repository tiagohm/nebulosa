package nebulosa.alignment.polar.point.three

internal data class Line(@JvmField val slope: Double, @JvmField val intercept: Double) {

    inline val isVertical
        get() = !slope.isFinite()

    inline val isHorizontal
        get() = slope == 0.0

    fun intersectionWith(other: Line): DoubleArray {
        val slope2 = other.slope
        val intercept2 = other.intercept

        val vertical1 = isVertical
        val vertical2 = other.isVertical

        if (slope == slope2 || (vertical1 && vertical2)) {
            throw IllegalArgumentException("identical lines do not have an intersection point.")
        } else if (vertical1) {
            return doubleArrayOf(intercept, slope2 * intercept + intercept2)
        } else if (vertical2) {
            return doubleArrayOf(intercept2, slope * intercept2 + intercept)
        } else {
            val x = (intercept2 - intercept) / (slope - slope2)
            return doubleArrayOf(x, slope * x + intercept)
        }
    }

    companion object {

        @JvmStatic
        fun fromPoints(p0: DoubleArray, p1: DoubleArray): Line {
            return fromPoints(p0[0], p0[1], p1[0], p1[1])
        }

        @JvmStatic
        fun fromPoints(x0: Double, y0: Double, x1: Double, y1: Double): Line {
            val k = (y1 - y0) / (x1 - x0)
            val b = if (!k.isFinite()) x0 else y0 - k * x0
            return Line(k, b)
        }
    }
}
