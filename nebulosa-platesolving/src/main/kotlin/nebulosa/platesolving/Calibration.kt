package nebulosa.platesolving

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nom.tam.fits.Header
import kotlin.math.hypot

data class Calibration(
    val solved: Boolean = false,
    val orientation: Angle = Angle.ZERO, // CROTA2
    val scale: Angle = Angle.ZERO, // CDELT2
    val rightAscension: Angle = Angle.ZERO, // CRVAL1
    val declination: Angle = Angle.ZERO, // CRVAL2
    val width: Angle = Angle.ZERO,
    val height: Angle = Angle.ZERO,
    val radius: Angle = hypot(width.value, height.value).rad / 2.0,
) : Header()
