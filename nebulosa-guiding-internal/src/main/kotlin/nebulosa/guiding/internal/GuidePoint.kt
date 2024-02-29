package nebulosa.guiding.internal

import nebulosa.math.Angle
import nebulosa.math.Point2D
import nebulosa.math.rad
import kotlin.math.atan2
import kotlin.math.hypot

interface GuidePoint : Point2D {

    val valid: Boolean

    fun dX(point: Point2D): Double {
        return x - point.x
    }

    fun dY(point: Point2D): Double {
        return y - point.y
    }

    val distance
        get() = hypot(x, y)

    val angle
        get() = atan2(y, x).rad

    fun angle(point: Point2D): Angle {
        return atan2(dY(point), dX(point)).rad
    }
}
