package nebulosa.imaging

import nom.tam.fits.BasicHDU
import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.ObservationDescription
import nom.tam.fits.header.Standard
import nom.tam.fits.header.extra.SBFitsExt

@Suppress("NOTHING_TO_INLINE")
inline fun Fits.imageHDU(n: Int) = read().filterIsInstance<ImageHDU>().getOrNull(n)

@Suppress("NOTHING_TO_INLINE")
inline fun BasicHDU<*>.naxis(n: Int) = header.getIntValue(Standard.NAXISn.n(n))

inline val Header.ra: String?
    get() = getStringValue(ObservationDescription.RA)
        ?: getStringValue(SBFitsExt.OBJCTRA)

inline val Header.dec: String?
    get() = getStringValue(ObservationDescription.DEC)
        ?: getStringValue(SBFitsExt.OBJCTDEC)
