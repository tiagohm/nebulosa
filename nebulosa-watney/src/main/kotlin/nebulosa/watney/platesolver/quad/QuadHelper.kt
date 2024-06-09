package nebulosa.watney.platesolver.quad

import nebulosa.constants.PIOVERTWO
import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun isCellInSearchRadius(radius: Angle, centerRA: Angle, centerDEC: Angle, bounds: CoordinateBounds): Boolean {
    val searchTopDEC = centerDEC + radius
    val searchBottomDEC = centerDEC - radius

    if (bounds.top < searchBottomDEC || bounds.bottom > searchTopDEC)
        return false

    var cellNearestPointDEC = max(bounds.bottom, min(centerDEC, bounds.top))

    // A bit hacky, but if we're at the pole, any cell that is at the pole is naturally in range.
    if (searchTopDEC.toDegrees >= 90.0 && bounds.top.toDegrees == 90.0)
        cellNearestPointDEC = PIOVERTWO

    val cellNearestPointRA = if (centerRA > bounds.left && centerRA < bounds.right) centerRA
    else if (angleDiff(centerRA, bounds.left) < angleDiff(centerRA, bounds.right)) bounds.left
    else bounds.right

    return cellNearestPointRA == centerRA && cellNearestPointDEC == centerDEC ||
            (radius - SphericalCoordinate.angularDistance(centerRA, centerDEC, cellNearestPointRA, cellNearestPointDEC)) > 0.0
}

private fun angleDiff(a: Angle, b: Angle): Double {
    val diff = (a.toDegrees - b.toDegrees + 180) % 360 - 180
    return abs(if (diff < -180) diff + 360 else diff)
}
