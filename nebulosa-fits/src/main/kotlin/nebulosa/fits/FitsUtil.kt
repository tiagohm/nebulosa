package nebulosa.fits

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter
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
inline fun Header.naxis() = getIntValue(Standard.NAXIS, -1)

@Suppress("NOTHING_TO_INLINE")
inline fun Header.naxis(n: Int) = getIntValue(Standard.NAXISn.n(n))

@Suppress("NOTHING_TO_INLINE")
inline fun Header.clone() = Header(makeData())

val Header.ra: Angle?
    get() = Angle.from(getStringValue(ObservationDescription.RA), true, false).takeIf { it.valid }
        ?: Angle.from(getStringValue(SBFitsExt.OBJCTRA), true).takeIf { it.valid }
        ?: getDoubleValue(NOAOExt.CRVAL1, Double.NaN).let { if (it.isFinite()) it.deg else null }

val Header.dec: Angle?
    get() = Angle.from(getStringValue(ObservationDescription.DEC)).takeIf { it.valid }
        ?: Angle.from(getStringValue(SBFitsExt.OBJCTDEC)).takeIf { it.valid }
        ?: getDoubleValue(NOAOExt.CRVAL2, Double.NaN).let { if (it.isFinite()) it.deg else null }

val FITS_RA_ANGLE_FORMATTER = AngleFormatter.HMS.newBuilder()
    .secondsDecimalPlaces(2)
    .whitespaced()
    .build()

val FITS_DEC_ANGLE_FORMATTER = AngleFormatter.SIGNED_DMS.newBuilder()
    .degreesFormat("%02d")
    .secondsDecimalPlaces(2)
    .whitespaced()
    .build()
