package nebulosa.fits

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.ObservationDescription
import nom.tam.fits.header.Standard
import nom.tam.fits.header.extra.NOAOExt
import nom.tam.fits.header.extra.SBFitsExt

@Suppress("NOTHING_TO_INLINE")
inline fun Fits.imageHDU(n: Int) = read().filterIsInstance<ImageHDU>().getOrNull(n)

@Suppress("NOTHING_TO_INLINE")
inline fun Header.naxis(n: Int) = getIntValue(Standard.NAXISn.n(n))

inline val Header.ra: Angle?
    get() = Angle.from(getStringValue(ObservationDescription.RA), true)
        ?: Angle.from(getStringValue(SBFitsExt.OBJCTRA), true)
        ?: getDoubleValue(NOAOExt.CRVAL1, Double.NaN).let { if (it.isFinite()) it.deg else null }

inline val Header.dec: Angle?
    get() = Angle.from(getStringValue(ObservationDescription.DEC))
        ?: Angle.from(getStringValue(SBFitsExt.OBJCTDEC))
        ?: getDoubleValue(NOAOExt.CRVAL2, Double.NaN).let { if (it.isFinite()) it.deg else null }
