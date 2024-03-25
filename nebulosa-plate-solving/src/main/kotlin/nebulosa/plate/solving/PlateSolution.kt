package nebulosa.plate.solving

import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeywordDictionary
import nebulosa.image.format.HeaderCard
import nebulosa.image.format.ReadableHeader
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
) : FitsHeader.ReadOnly(header) {

    override fun readOnly() = this

    companion object {

        @JvmStatic val NO_SOLUTION = PlateSolution()

        @JvmStatic private val LOG = loggerFor<PlateSolution>()

        @JvmStatic
        fun from(header: ReadableHeader): PlateSolution? {
            val (cd11, cd12, _, cd22) = header.computeCdMatrix()
            val crota2 = header.getDoubleOrNull(FitsKeywordDictionary.CROTA2)?.deg ?: atan2(cd12, cd11).rad
            // https://danmoser.github.io/notes/gai_fits-imgs.html
            val cdelt1 = header.getDoubleOrNull(FitsKeywordDictionary.CDELT1)?.deg ?: (cd11 / cos(crota2)).deg
            val cdelt2 = header.getDoubleOrNull(FitsKeywordDictionary.CDELT2)?.deg ?: (cd22 / cos(crota2)).deg
            val crval1 = header.getDoubleOrNull(FitsKeywordDictionary.CRVAL1)?.deg ?: return null
            val crval2 = header.getDoubleOrNull(FitsKeywordDictionary.CRVAL2)?.deg ?: return null
            val width = header.getIntOrNull(FitsKeywordDictionary.NAXIS1) ?: header.getInt("IMAGEW", 0)
            val height = header.getIntOrNull(FitsKeywordDictionary.NAXIS2) ?: header.getInt("IMAGEH", 0)

            LOG.info(
                "solution from {}: ORIE={}, SCALE={}, RA={}, DEC={}",
                header, crota2.formatSignedDMS(), cdelt2.toArcsec,
                crval1.formatHMS(), crval2.formatSignedDMS(),
            )

            return PlateSolution(true, crota2, cdelt2, crval1, crval2, abs(cdelt1 * width), abs(cdelt2 * height), header = header)
        }
    }
}
