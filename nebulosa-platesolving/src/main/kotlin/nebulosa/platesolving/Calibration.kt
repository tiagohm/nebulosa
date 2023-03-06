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
) : HashMap<String, Any>() {

    init {
        if (hasWCS) {
            this["CTYPE1"] = ctype1
            this["CTYPE2"] = ctype2
            this["CRPIX1"] = crpix1
            this["CRPIX2"] = crpix2
            this["CRVAL1"] = crval1.degrees
            this["CRVAL2"] = crval2.degrees
            this["CDELT1"] = cdelt1.degrees
            this["CDELT2"] = cdelt2.degrees
            this["CROTA1"] = crota1.degrees
            this["CROTA2"] = crota2.degrees
            this["CD1_1"] = cd11
            this["CD1_2"] = cd12
            this["CD2_1"] = cd21
            this["CD2_2"] = cd22
        }
    }

    val radius = hypot(width.value, height.value).rad / 2.0

    companion object {

        @JvmStatic val EMPTY = Calibration()
    }
}
