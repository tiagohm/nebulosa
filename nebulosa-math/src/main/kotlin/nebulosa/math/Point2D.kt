package nebulosa.math

import kotlin.math.hypot

interface Point2D {

    val x: Double

    val y: Double

    operator fun component1() = x

    operator fun component2() = y

    fun distance(other: Point2D): Double {
        return hypot(x - other.x, y - other.y)
    }
}
