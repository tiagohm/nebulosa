package nebulosa.platesolving

import nebulosa.math.Angle

data class Calibration(
    val orientation: Angle = Angle.ZERO,
    val scale: Double = 0.0,
    val radius: Angle = Angle.ZERO,
    val ra: Angle = Angle.ZERO,
    val dec: Angle = Angle.ZERO,
)
