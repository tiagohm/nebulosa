package nebulosa.plate.solving

import nebulosa.fits.Header
import nebulosa.fits.HeaderCard
import nebulosa.fits.ReadOnlyHeader
import nebulosa.fits.Standard
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.wcs.computeCdMatrix
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
    private val header: Collection<HeaderCard> = emptyList(),
) : ReadOnlyHeader(header) {

    override fun readOnly() = this

    companion object {

        @JvmStatic val NO_SOLUTION = PlateSolution()

        @JvmStatic private val LOG = loggerFor<PlateSolution>()

        @JvmStatic
        fun from(header: Header): PlateSolution? {
            val (cd11, cd12, _, cd22) = header.computeCdMatrix()
            val crota2 = header.getDoubleOrNull(Standard.CROTA2)?.deg ?: atan2(cd12, cd11).rad
            // https://danmoser.github.io/notes/gai_fits-imgs.html
            val cdelt1 = header.getDoubleOrNull(Standard.CDELT1)?.deg ?: (cd11 / cos(crota2)).deg
            val cdelt2 = header.getDoubleOrNull(Standard.CDELT2)?.deg ?: (cd22 / cos(crota2)).deg
            val crval1 = header.getDoubleOrNull(Standard.CRVAL1)?.deg ?: return null
            val crval2 = header.getDoubleOrNull(Standard.CRVAL2)?.deg ?: return null
            val width = header.getIntOrNull(Standard.NAXIS1) ?: header.getInt("IMAGEW", 0)
            val height = header.getIntOrNull(Standard.NAXIS2) ?: header.getInt("IMAGEH", 0)

            LOG.info(
                "solution from {}: ORIE={}, SCALE={}, RA={}, DEC={}",
                header, crota2.format(AngleFormatter.SIGNED_DMS), cdelt2.toArcsec,
                crval1.format(AngleFormatter.HMS), crval2.format(AngleFormatter.SIGNED_DMS),
            )

            return PlateSolution(true, crota2, cdelt2, crval1, crval2, abs(cdelt1 * width), abs(cdelt2 * height), header = header)
        }
    }
}
