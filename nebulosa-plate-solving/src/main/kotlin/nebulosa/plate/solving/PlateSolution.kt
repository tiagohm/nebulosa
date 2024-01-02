package nebulosa.plate.solving

import nebulosa.fits.Header
import nebulosa.fits.NOAOExt
import nebulosa.fits.Standard
import nebulosa.log.loggerFor
import nebulosa.math.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
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

    override fun toString() = "PlateSolution(solved=$solved, orientation=$orientation, scale=$scale, " +
            "rightAscension=$rightAscension, declination=$declination, width=$width, " +
            "height=$height, parity=$parity, radius=$radius, header=${super.toString()})"

    companion object {

        @JvmStatic val NO_SOLUTION = PlateSolution()

        @JvmStatic private val LOG = loggerFor<PlateSolution>()

        @JvmStatic
        fun from(header: Header): PlateSolution? {
            val cd11 = header.getDoubleOrNull(NOAOExt.CD1_1)
            val cd22 = header.getDoubleOrNull(NOAOExt.CD2_2)
            val cd12 = header.getDoubleOrNull(NOAOExt.CD1_2)
            val crota2 = header.getDoubleOrNull(Standard.CROTA2)?.deg ?: atan2(cd12 ?: return null, cd11 ?: return null).rad
            // https://danmoser.github.io/notes/gai_fits-imgs.html
            val cdelt1 = header.getDoubleOrNull(Standard.CDELT1)?.deg ?: ((cd11 ?: return null) / cos(crota2)).deg
            val cdelt2 = header.getDoubleOrNull(Standard.CDELT2)?.deg ?: ((cd22 ?: return null) / cos(crota2)).deg
            val crval1 = header.getDoubleOrNull(Standard.CRVAL1)?.deg ?: return null
            val crval2 = header.getDoubleOrNull(Standard.CRVAL2)?.deg ?: return null
            val width = header.getIntOrNull(Standard.NAXIS1) ?: header.getInt("IMAGEW", 0)
            val height = header.getIntOrNull(Standard.NAXIS2) ?: header.getInt("IMAGEH", 0)

            LOG.info(
                "solution from {}: ORIE={}, SCALE={}, RA={}, DEC={}",
                header, crota2.format(AngleFormatter.SIGNED_DMS), cdelt2.toArcsec,
                crval1.format(AngleFormatter.HMS), crval2.format(AngleFormatter.SIGNED_DMS),
            )

            val solution = PlateSolution(true, crota2, cdelt2, crval1, crval2, abs(cdelt1 * width), abs(cdelt2 * height))
            header.iterator().forEach(solution::add)
            return solution
        }
    }
}
