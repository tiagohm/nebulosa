package nebulosa.plate.solving

import nebulosa.fits.Header
import nebulosa.fits.NOAOExt
import nebulosa.fits.Standard
import nebulosa.log.loggerFor
import nebulosa.math.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

data class PlateSolution(
    val solved: Boolean = false,
    val orientation: Angle = 0.0, // CROTA2
    val scale: Angle = 0.0, // CDELT2
    val rightAscension: Angle = 0.0, // CRVAL1
    val declination: Angle = 0.0, // CRVAL2
    val width: Angle = 0.0,
    val height: Angle = 0.0,
    val parity: Parity = Parity.NORMAL,
    val radius: Angle = hypot(width, height).rad / 2.0,
) : Header() {

    companion object {

        @JvmStatic val NO_SOLUTION = PlateSolution()

        @JvmStatic private val LOG = loggerFor<PlateSolution>()

        @JvmStatic
        fun from(header: Header): PlateSolution? {
            val cd11 = header.getDouble(NOAOExt.CD1_1, Double.NaN)
            val crota2 = header.getDouble(Standard.CROTA2, Double.NaN).takeIf(Double::isFinite)?.deg
                ?: atan2(header.getDouble(NOAOExt.CD1_2, Double.NaN), cd11).rad
            // https://danmoser.github.io/notes/gai_fits-imgs.html
            val cdelt1 = header.getDouble(Standard.CDELT1, cd11).takeIf(Double::isFinite)?.deg ?: return null
            val cdelt2 = header.getDouble(Standard.CDELT2, header.getDouble(NOAOExt.CD2_2, Double.NaN)).takeIf(Double::isFinite)?.deg ?: return null
            val crval1 = header.getDouble(Standard.CRVAL1, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val crval2 = header.getDouble(Standard.CRVAL2, Double.NaN).takeIf(Double::isFinite)?.deg ?: return null
            val width = header.getInt(Standard.NAXIS1, 0)
            val height = header.getInt(Standard.NAXIS2, 0)

            LOG.info(
                "solution from header. ORIE={}, SCALE={}, RA={}, DEC={}",
                crota2.format(AngleFormatter.SIGNED_DMS), cdelt2.toArcsec,
                crval1.format(AngleFormatter.HMS), crval2.format(AngleFormatter.SIGNED_DMS),
            )

            val solution = PlateSolution(true, crota2, cdelt2, crval1, crval2, abs(cdelt1 * width), abs(cdelt2 * height))
            header.iterator().forEach(solution::add)
            return solution
        }
    }
}
