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
            val cd11 = header.getDoubleOrNull(NOAOExt.CD1_1)
            val cd12 = header.getDoubleOrNull(NOAOExt.CD1_2)
            val crota2 = header.getDoubleOrNull(Standard.CROTA2)?.deg ?: if (cd11 != null && cd12 != null) atan2(cd12, cd11).rad else 0.0
            // https://danmoser.github.io/notes/gai_fits-imgs.html
            val cdelt1 = header.getDouble(Standard.CDELT1, cd11 ?: 0.0).deg
            val cdelt2 = header.getDoubleOrNull(Standard.CDELT2)?.deg ?: header.getDoubleOrNull(NOAOExt.CD2_2)?.deg ?: return null
            val crval1 = header.getDoubleOrNull(Standard.CRVAL1)?.deg ?: return null
            val crval2 = header.getDoubleOrNull(Standard.CRVAL2)?.deg ?: return null
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
