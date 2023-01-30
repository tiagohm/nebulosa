package nebulosa.guiding.local

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Represents a location on a guide camera image.
 */
@Suppress("NOTHING_TO_INLINE")
open class Point(
    x: Float,
    y: Float,
) {

    var x = x
        internal set(value) {
            field = value
            isValid = true
        }

    var y = y
        internal set(value) {
            field = value
            isValid = true
        }

    var isValid = true
        internal set

    constructor(point: Point) : this(point.x, point.y) {
        isValid = point.isValid
    }

    inline fun dX(point: Point) = x - point.x

    inline fun dY(point: Point) = y - point.y

    inline val distance get() = hypot(x, y)

    inline fun distance(point: Point) = hypot(dX(point), dY(point))

    inline val angle get() = angle(ZERO)

    fun angle(point: Point): Angle {
        val dx = dX(point)
        val dy = dY(point)
        return if (dx != 0f || dy != 0f) atan2(dy, dx).rad
        else Angle.ZERO
    }

    open fun invalidate() {
        isValid = false
    }

    inline operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    inline operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    companion object {

        @JvmStatic val NONE = Point(Float.NaN, Float.NaN)
        @JvmStatic val ZERO = Point(0f, 0f)
    }
}
