package nebulosa.platesolver

import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeyword
import nebulosa.image.format.HeaderCard
import nebulosa.image.format.ReadableHeader
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.wcs.computeCdMatrix
import kotlin.math.*

data class PlateSolution(
    @JvmField val solved: Boolean = false,
    @JvmField val orientation: Angle = 0.0, // CROTA2
    @JvmField val scale: Angle = 0.0, // CDELT2
    @JvmField val rightAscension: Angle = 0.0, // CRVAL1
    @JvmField val declination: Angle = 0.0, // CRVAL2
    @JvmField val width: Angle = 0.0,
    @JvmField val height: Angle = 0.0,
    @JvmField val parity: Parity = Parity.NORMAL,
    @JvmField val radius: Angle = hypot(width, height).rad / 2.0,
    @JvmField val widthInPixels: Double = truncate(width / scale),
    @JvmField val heightInPixels: Double = truncate(height / scale),
    private val header: Collection<HeaderCard> = emptyList(),
) : FitsHeader.ReadOnly(header) {

    override fun readOnly() = this

    companion object {

        @JvmStatic val NO_SOLUTION = PlateSolution()

        @JvmStatic private val LOG = loggerFor<PlateSolution>()

        @JvmStatic
        fun from(header: ReadableHeader): PlateSolution? {
            val crval1 = header.getDoubleOrNull(FitsKeyword.CRVAL1)?.deg ?: return null
            val crval2 = header.getDoubleOrNull(FitsKeyword.CRVAL2)?.deg ?: return null
            val (cd11, cd12, _, cd22) = header.computeCdMatrix()
            val crota2 = header.getDoubleOrNull(FitsKeyword.CROTA2)?.deg ?: atan2(cd12, cd11).rad
            // https://danmoser.github.io/notes/gai_fits-imgs.html
            val cdelt1 = header.getDoubleOrNull(FitsKeyword.CDELT1)?.deg ?: (cd11 / cos(crota2)).deg
            val cdelt2 = header.getDoubleOrNull(FitsKeyword.CDELT2)?.deg ?: (cd22 / cos(crota2)).deg
            val width = header.getIntOrNull(FitsKeyword.NAXIS1) ?: header.getInt("IMAGEW", 0)
            val height = header.getIntOrNull(FitsKeyword.NAXIS2) ?: header.getInt("IMAGEH", 0)

            LOG.info(
                "solution from {}: ORIE={}, SCALE={}, RA={}, DEC={}",
                header, crota2.formatSignedDMS(), cdelt2.toArcsec,
                crval1.formatHMS(), crval2.formatSignedDMS(),
            )

            return PlateSolution(
                true, crota2, cdelt2, crval1, crval2, abs(cdelt1 * width), abs(cdelt2 * height),
                widthInPixels = width.toDouble(), heightInPixels = height.toDouble(), header = header
            )
        }
    }
}
