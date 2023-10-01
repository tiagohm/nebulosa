package nebulosa.wcs

import nebulosa.math.Angle

data class PixelCoordinates(
    val x: Double,
    val y: Double,
    val phi: Angle,
    val theta: Angle,
)
