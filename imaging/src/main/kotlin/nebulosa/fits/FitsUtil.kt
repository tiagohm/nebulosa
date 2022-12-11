package nebulosa.fits

import nom.tam.fits.BasicHDU
import nom.tam.fits.Fits
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.Standard

@Suppress("NOTHING_TO_INLINE")
inline fun Fits.imageHDU(n: Int) = read().filterIsInstance<ImageHDU>().getOrNull(n)

@Suppress("NOTHING_TO_INLINE")
inline fun BasicHDU<*>.naxis(n: Int) = header.getIntValue(Standard.NAXISn.n(n))
