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

val Header.rightAscension
    get() = Angle(getString(Standard.RA, ""), isHours = true, decimalIsHours = false)
        .takeIf { it.isFinite() }
        ?.let { Angle(getString(SBFitsExt.OBJCTRA, ""), true) }
        ?.takeIf { it.isFinite() }
        ?: getDouble(NOAOExt.CRVAL1, Double.NaN).deg

val Header.declination
    get() = Angle(getString(Standard.DEC, ""))
        .takeIf { it.isFinite() }
        ?.let { Angle(getString(SBFitsExt.OBJCTDEC, "")) }
        ?.takeIf { it.isFinite() }
        ?: getDouble(NOAOExt.CRVAL2, Double.NaN).deg

val Header.binX
    get() = getInt(SBFitsExt.XBINNING, 1)

val Header.binY
    get() = getInt(SBFitsExt.YBINNING, 1)

val Header.exposureTime
    get() = getDouble(Standard.EXPTIME, getDouble(Standard.EXPOSURE, 0.0)).seconds

val Header.temperature
    get() = getDouble(NOAOExt.CCDTEM, -272.15)

val Header.latitude
    get() = getDouble(SBFitsExt.SITELAT, getDouble("LAT-OBS", Double.NaN)).deg

val Header.longitude
    get() = getDouble(SBFitsExt.SITELONG, getDouble("LONG-OBS", Double.NaN)).deg

val Header.observationDate
    get() = getString(Standard.DATE_OBS, "")
        .ifBlank { null }
        ?.let(LocalDateTime::parse)

val Header.cfaPattern
    get() = getString(MaxImDLExt.BAYERPAT, "").ifBlank { null }?.trim()
