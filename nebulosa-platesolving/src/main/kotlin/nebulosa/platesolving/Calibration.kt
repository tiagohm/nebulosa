package nebulosa.platesolving

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import kotlin.math.hypot

data class Calibration(
    // WCS.
    val hasWCS: Boolean = false,
    val ctype1: String = "",
    val ctype2: String = "",
    val crpix1: Double = 0.0,
    val crpix2: Double = 0.0,
    val crval1: Angle = Angle.ZERO,
    val crval2: Angle = Angle.ZERO,
    val cdelt1: Angle = Angle.ZERO,
    val cdelt2: Angle = Angle.ZERO,
    val crota1: Angle = Angle.ZERO,
    val crota2: Angle = Angle.ZERO,
    val cd11: Double = 0.0,
    val cd12: Double = 0.0,
    val cd21: Double = 0.0,
    val cd22: Double = 0.0,
    // Calibration.
    val orientation: Angle = crota2,
    val scale: Angle = cdelt2,
    val rightAscension: Angle = crval1,
    val declination: Angle = crval2,
    val width: Angle = Angle.ZERO,
    val height: Angle = Angle.ZERO,
) {

    val radius = hypot(width.value, height.value).rad / 2.0

    companion object {

        @JvmStatic val EMPTY = Calibration()
    }
}
