package nebulosa.guiding.internal

import nebulosa.math.Angle
import kotlin.math.hypot

interface GuidePoint {

    val x: Double

    val y: Double

    val valid: Boolean

    fun dX(point: GuidePoint): Double

    fun dY(point: GuidePoint): Double

    val distance: Double

    fun distance(point: GuidePoint) = hypot(dX(point), dY(point))

    val angle: Angle

    fun angle(point: GuidePoint): Angle
}
