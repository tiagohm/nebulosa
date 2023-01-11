package nebulosa.guiding

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Represents a location on a guide camera image.
 */
@Suppress("NOTHING_TO_INLINE")
open class Point(
    @Volatile @JvmField var x: Float,
    @Volatile @JvmField var y: Float,
    @Volatile @JvmField var valid: Boolean = true,
) {

    constructor(point: Point) : this(point.x, point.y, point.valid)

    inline fun dX(point: Point) = x - point.x

    inline fun dY(point: Point) = y - point.y

    inline val distance get() = hypot(x, y)

    inline fun distance(point: Point) = hypot(dX(point), dY(point))

    inline val angle get() = angle(ZERO)

    fun angle(point: Point): Angle {
        val dx = dX(point)
        val dy = dY(point)
        return if (dx != 0f || dy != 0f) atan2(dy, dx).rad else Angle.ZERO
    }

    open fun invalidate() {
        valid = false
    }

    companion object {

        @JvmStatic val NONE = Point(Float.MIN_VALUE, Float.MIN_VALUE)

        @JvmStatic val ZERO = Point(0f, 0f)
    }
}
