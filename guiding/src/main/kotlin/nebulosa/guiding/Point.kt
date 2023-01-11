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
    @Volatile @JvmField var x: Int,
    @Volatile @JvmField var y: Int,
    @Volatile @JvmField var valid: Boolean = true,
) {

    constructor(point: Point) : this(point.x, point.y, point.valid)

    inline fun dX(point: Point) = x - point.x

    inline fun dY(point: Point) = y - point.y

    inline val distance get() = hypot(x.toFloat(), y.toFloat())

    inline fun distance(point: Point) = hypot(dX(point).toFloat(), dY(point).toFloat())

    inline val angle get() = angle(ZERO)

    fun angle(point: Point): Angle {
        val dx = dX(point)
        val dy = dY(point)
        return if (dx != 0 || dy != 0) atan2(dy.toFloat(), dx.toFloat()).rad
        else Angle.ZERO
    }

    open fun invalidate() {
        valid = false
    }

    companion object {

        @JvmStatic val NONE = Point(Int.MIN_VALUE, Int.MIN_VALUE)

        @JvmStatic val ZERO = Point(0, 0)
    }
}
