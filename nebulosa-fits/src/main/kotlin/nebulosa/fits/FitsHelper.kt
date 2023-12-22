package nebulosa.fits

import nebulosa.math.Angle
import nebulosa.math.deg
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@Suppress("NOTHING_TO_INLINE")
inline fun Header.clone() = Header(this)

inline val Header.naxis
    get() = getInt(Standard.NAXIS, -1)

@Suppress("NOTHING_TO_INLINE")
inline fun Header.naxis(n: Int) = getInt(Standard.NAXISn.n(n), 0)

inline val Header.width
    get() = naxis(1)

inline val Header.height
    get() = naxis(2)

val Header.rightAscension
    get() = Angle(getStringOrNull(Standard.RA), isHours = true, decimalIsHours = false).takeIf { it.isFinite() }
        ?: Angle(getStringOrNull(SBFitsExt.OBJCTRA), true).takeIf { it.isFinite() }
        ?: getDouble(NOAOExt.CRVAL1, Double.NaN).deg

val Header.declination
    get() = Angle(getStringOrNull(Standard.DEC)).takeIf { it.isFinite() }
        ?: Angle(getStringOrNull(SBFitsExt.OBJCTDEC)).takeIf { it.isFinite() }
        ?: getDouble(NOAOExt.CRVAL2, Double.NaN).deg

inline val Header.binX
    get() = getInt(SBFitsExt.XBINNING, 1)

inline val Header.binY
    get() = getIntOrNull(SBFitsExt.YBINNING) ?: binX

val Header.exposureTime
    get() = (getDoubleOrNull(Standard.EXPTIME) ?: getDouble(Standard.EXPOSURE, 0.0)).seconds

inline val Header.exposureTimeInMicroseconds
    get() = exposureTime.inWholeMicroseconds

const val INVALID_TEMPERATURE = 999.0

inline val Header.temperature
    get() = getDouble(NOAOExt.CCDTEM, INVALID_TEMPERATURE)

inline val Header.gain
    get() = getDouble(NOAOExt.GAIN, 0.0)

val Header.latitude
    get() = (getDoubleOrNull(SBFitsExt.SITELAT) ?: getDoubleOrNull("LAT-OBS"))?.deg

val Header.longitude
    get() = (getDoubleOrNull(SBFitsExt.SITELONG) ?: getDoubleOrNull("LONG-OBS"))?.deg

val Header.observationDate
    get() = getStringOrNull(Standard.DATE_OBS)?.let(LocalDateTime::parse)

val Header.cfaPattern
    get() = getStringOrNull(MaxImDLExt.BAYERPAT)?.ifBlank { null }?.trim()

val Header.filter
    get() = getStringOrNull(Standard.FILTER)?.ifBlank { null }?.trim()

val Header.frame
    get() = (getStringOrNull("FRAME") ?: getStringOrNull(SBFitsExt.IMAGETYP))?.ifBlank { null }

val Header.instrument
    get() = getStringOrNull(Standard.INSTRUME)?.ifBlank { null }
