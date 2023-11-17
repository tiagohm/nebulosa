package nebulosa.platesolving

import nebulosa.fits.Header
import nebulosa.fits.Standard
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.rad
import kotlin.math.hypot

data class Calibration(
    val solved: Boolean = false,
    val orientation: Angle = 0.0, // CROTA2
    val scale: Angle = 0.0, // CDELT2
    val rightAscension: Angle = 0.0, // CRVAL1
    val declination: Angle = 0.0, // CRVAL2
    val width: Angle = 0.0,
    val height: Angle = 0.0,
    val radius: Angle = hypot(width, height).rad / 2.0,
) : Header() {

    companion object {

        @JvmStatic
        fun from(header: Header): Calibration? {
            val crota2 = header.getDouble(Standard.CROTA2, 0.0).deg
            val cdelt1 = header.getDouble(Standard.CDELT1, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val cdelt2 = header.getDouble(Standard.CDELT2, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val crval1 = header.getDouble(Standard.CRVAL1, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val crval2 = header.getDouble(Standard.CRVAL2, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val width = header.getInt(Standard.NAXIS1, 0)
            val height = header.getInt(Standard.NAXIS2, 0)
            val calibration = Calibration(true, crota2, cdelt2, crval1, crval2, cdelt1 * width, cdelt2 * height)
            header.iterator().forEach(calibration::add)
            return calibration
        }
    }
}
