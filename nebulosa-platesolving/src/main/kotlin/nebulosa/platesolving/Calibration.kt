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
    val hasCD: Boolean = false,
    val cd11: Double = 0.0,
    val cd12: Double = 0.0,
    val cd21: Double = 0.0,
    val cd22: Double = 0.0,
    val hasPC: Boolean = false,
    val pc11: Double = 0.0,
    val pc12: Double = 0.0,
    val pc21: Double = 0.0,
    val pc22: Double = 0.0,
    val pv11: Angle = Angle.NaN,
    val pv12: Angle = Angle.NaN,
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

            if (hasCD) {
                this["CD1_1"] = cd11
                this["CD1_2"] = cd12
                this["CD2_1"] = cd21
                this["CD2_2"] = cd22
            }

            if (hasPC) {
                this["PC1_1"] = pc11
                this["PC1_2"] = pc12
                this["PC2_1"] = pc21
                this["PC2_2"] = pc22
            }

            if (pv11.valid) this["PV1_1"] = pv11.degrees
            if (pv12.valid) this["PV1_2"] = pv12.degrees
        }
    }

    val radius = hypot(width.value, height.value).rad / 2.0

    companion object {

        @JvmStatic val EMPTY = Calibration()
    }
}
