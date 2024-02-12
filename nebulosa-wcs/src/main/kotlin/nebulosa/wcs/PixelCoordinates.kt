package nebulosa.wcs

import nebulosa.math.Angle
import nebulosa.math.Point2D

data class PixelCoordinates(override val x: Double, override val y: Double, val phi: Angle, val theta: Angle) : Point2D
