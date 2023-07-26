package nebulosa.fits

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter
import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageHDU

fun Fits.imageHDU(n: Int): ImageHDU? {
    var index = 0

    for (i in 0..255) {
        val hdu = getHDU(i) ?: break

        if (hdu is ImageHDU && index++ == n) {
            return hdu
        }
    }

    return null
}

@Suppress("NOTHING_TO_INLINE")
inline fun Header.naxis() = getIntValue(FitsKeywords.NAXIS, -1)

@Suppress("NOTHING_TO_INLINE")
inline fun Header.naxis(n: Int) = getIntValue(FitsKeywords.NAXISn.n(n))

@Suppress("NOTHING_TO_INLINE")
inline fun Header.clone() = Header(makeData())

val Header.ra
    get() = Angle.from(getStringValue(FitsKeywords.RA), true, false).takeIf { it.valid }
        ?: Angle.from(getStringValue(FitsKeywords.OBJCTRA), true).takeIf { it.valid }
        ?: getDoubleValue(FitsKeywords.CRVAL1, Double.NaN).let { if (it.isFinite()) it.deg else null }

val Header.dec
    get() = Angle.from(getStringValue(FitsKeywords.DEC)).takeIf { it.valid }
        ?: Angle.from(getStringValue(FitsKeywords.OBJCTDEC)).takeIf { it.valid }
        ?: getDoubleValue(FitsKeywords.CRVAL2, Double.NaN).let { if (it.isFinite()) it.deg else null }

val FITS_RA_ANGLE_FORMATTER = AngleFormatter.HMS.newBuilder()
    .secondsDecimalPlaces(2)
    .whitespaced()
    .build()

val FITS_DEC_ANGLE_FORMATTER = AngleFormatter.SIGNED_DMS.newBuilder()
    .degreesFormat("%02d")
    .secondsDecimalPlaces(2)
    .whitespaced()
    .build()
