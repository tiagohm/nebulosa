package nebulosa.guiding.internal

import nebulosa.math.Point2D

interface GuidePoint : Point2D {

    val valid: Boolean
}
