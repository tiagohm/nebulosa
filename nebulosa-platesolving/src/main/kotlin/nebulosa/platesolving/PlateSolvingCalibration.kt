package nebulosa.platesolving

import nebulosa.math.Angle

data class PlateSolvingCalibration(
    val orientation: Angle = Angle.ZERO,
    val pixScale: Double = 0.0,
    val radius: Angle = Angle.ZERO,
    val ra: Angle = Angle.ZERO,
    val dec: Angle = Angle.ZERO,
)
