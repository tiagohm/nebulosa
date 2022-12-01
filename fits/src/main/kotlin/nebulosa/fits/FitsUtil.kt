package nebulosa.fits

import nom.tam.fits.BasicHDU
import nom.tam.fits.Fits
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.Standard

fun Fits.getImageHDU(n: Int) = read().filterIsInstance<ImageHDU>().getOrNull(n)

fun BasicHDU<*>.getNAXIS(n: Int) = header.getIntValue(Standard.NAXISn.n(n))
