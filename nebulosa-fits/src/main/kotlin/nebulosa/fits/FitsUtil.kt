package nebulosa.fits

import nebulosa.math.Angle
import nebulosa.math.deg
import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageHDU
import java.time.LocalDateTime

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
    get() = Angle(
        getStringValue(FitsKeywords.RA), isHours = true, decimalIsHours = false,
        defaultValue = Angle(
            getStringValue(FitsKeywords.OBJCTRA), true,
            defaultValue = getDoubleValue(FitsKeywords.CRVAL1, Double.NaN).deg
        )
    )

val Header.dec
    get() = Angle(
        getStringValue(FitsKeywords.DEC),
        defaultValue = Angle(
            getStringValue(FitsKeywords.OBJCTDEC),
            defaultValue = getDoubleValue(FitsKeywords.CRVAL2, Double.NaN).deg
        )
    )

val Header.latitude
    get() = getDoubleValue(FitsKeywords.SITELAT, getDoubleValue("LAT-OBS")).deg

val Header.longitude
    get() = getDoubleValue(FitsKeywords.SITELONG, getDoubleValue("LONG-OBS")).deg

val Header.observationDate
    get() = getStringValue(FitsKeywords.DATE_OBS)
        ?.ifBlank { null }
        ?.let(LocalDateTime::parse)
