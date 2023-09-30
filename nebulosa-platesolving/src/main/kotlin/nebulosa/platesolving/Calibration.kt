package nebulosa.platesolving

import nebulosa.fits.FitsKeywords
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.rad
import nom.tam.fits.Header
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
            val crota2 = header.getDoubleValue(FitsKeywords.CROTA2, 0.0).deg
            val cdelt1 = header.getDoubleValue(FitsKeywords.CDELT1, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val cdelt2 = header.getDoubleValue(FitsKeywords.CDELT2, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val crval1 = header.getDoubleValue(FitsKeywords.CRVAL1, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val crval2 = header.getDoubleValue(FitsKeywords.CRVAL2, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val width = header.getDoubleValue(FitsKeywords.NAXISn.n(1))
            val height = header.getDoubleValue(FitsKeywords.NAXISn.n(2))
            val calibration = Calibration(true, crota2, cdelt2, crval1, crval2, cdelt1 * width, cdelt2 * height)
            header.iterator().forEach(calibration::addLine)
            return calibration
        }
    }
}
