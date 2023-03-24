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
    x: Double = 0.0,
    y: Double = 0.0,
    valid: Boolean = true,
) {

    var valid = valid
        protected set

    var x = x
        protected set

    var y = y
        protected set

    constructor(point: Point) : this(point.x, point.y, point.valid)

    open fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
        valid = true
    }

    fun set(point: Point) {
        set(point.x, point.y)
    }

    fun dX(point: Point) = x - point.x

    fun dY(point: Point) = y - point.y

    inline val distance
        get() = hypot(x, y)

    fun distance(point: Point) = hypot(dX(point), dY(point))

    val angle
        get() = angle(ZERO)

    fun angle(point: Point): Angle {
        val dx = dX(point)
        val dy = dY(point)
        return if (dx != 0.0 || dy != 0.0) atan2(dy, dx).rad
        else Angle.ZERO
    }

    open fun invalidate() {
        valid = false
    }

    inline operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    inline operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    companion object {

        @JvmStatic private val ZERO = Point()
    }
}
