package nebulosa.math

import kotlin.math.atan2
import kotlin.math.hypot

interface Point2D {

    val x: Double

    val y: Double

    operator fun component1() = x

    operator fun component2() = y

    val length
        get() = hypot(x, y)

    fun distance(other: Point2D): Double {
        return hypot(x - other.x, y - other.y)
    }

    fun angle(other: Point2D): Angle {
        return atan2(other.y - y, other.x - x)
    }

    data class XY(override val x: Double, override val y: Double) : Point2D

    companion object {

        @JvmStatic val ZERO: Point2D = XY(0.0, 0.0)

        @JvmStatic
        operator fun invoke(x: Double, y: Double): Point2D = XY(x, y)
    }
}
