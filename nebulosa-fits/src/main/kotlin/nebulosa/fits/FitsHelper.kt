@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.fits

import nebulosa.image.format.ReadableHeader
import nebulosa.io.SeekableSource
import nebulosa.math.Angle
import nebulosa.math.deg
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

inline val ReadableHeader.naxis
    get() = getInt(FitsKeyword.NAXIS, -1)

inline fun ReadableHeader.naxis(n: Int) = getInt(FitsKeyword.NAXISn.n(n), 0)

inline val ReadableHeader.width
    get() = getInt(FitsKeyword.NAXIS1, 0)

inline val ReadableHeader.height
    get() = getInt(FitsKeyword.NAXIS2, 0)

inline val ReadableHeader.numberOfChannels
    get() = getInt(FitsKeyword.NAXIS3, 1)

inline val ReadableHeader.bitpix
    get() = Bitpix.from(this)

val ReadableHeader.rightAscension
    get() = Angle(getStringOrNull(FitsKeyword.RA), isHours = true, decimalIsHours = false).takeIf { it.isFinite() }
        ?: Angle(getStringOrNull(FitsKeyword.OBJCTRA), true).takeIf { it.isFinite() }
        ?: getDouble(FitsKeyword.CRVAL1, Double.NaN).deg

val ReadableHeader.declination
    get() = Angle(getStringOrNull(FitsKeyword.DEC)).takeIf { it.isFinite() }
        ?: Angle(getStringOrNull(FitsKeyword.OBJCTDEC)).takeIf { it.isFinite() }
        ?: getDouble(FitsKeyword.CRVAL2, Double.NaN).deg

inline val ReadableHeader.binX
    get() = getInt(FitsKeyword.XBINNING, 1)

inline val ReadableHeader.binY
    get() = getIntOrNull(FitsKeyword.YBINNING) ?: binX

inline val ReadableHeader.exposureTimeInSeconds
    get() = getDoubleOrNull(FitsKeyword.EXPTIME) ?: getDouble(FitsKeyword.EXPOSURE, 0.0)

inline val ReadableHeader.exposureTime: Duration
    get() = Duration.ofNanos((exposureTimeInSeconds * 1000000000.0).toLong())

inline val ReadableHeader.exposureTimeInMicroseconds
    get() = (exposureTimeInSeconds * 1000000.0).toLong()

const val INVALID_TEMPERATURE = 999.0

inline val ReadableHeader.temperature
    get() = getDoubleOrNull(FitsKeyword.CCDTEM) ?: getDouble(FitsKeyword.CCD_TEMP, INVALID_TEMPERATURE)

inline val ReadableHeader.gain
    get() = getDouble(FitsKeyword.GAIN, 0.0)

inline val ReadableHeader.latitude
    get() = (getDoubleOrNull(FitsKeyword.SITELAT)?.deg ?: getDoubleOrNull("LAT-OBS"))?.deg

inline val ReadableHeader.longitude
    get() = (getDoubleOrNull(FitsKeyword.SITELONG)?.deg ?: getDoubleOrNull("LONG-OBS"))?.deg

inline val ReadableHeader.observationDate
    get() = getStringOrNull(FitsKeyword.DATE_OBS)?.let(LocalDateTime::parse)

inline val ReadableHeader.cfaPattern
    get() = getStringOrNull(FitsKeyword.BAYERPAT)?.ifBlank { null }?.trim()

inline val ReadableHeader.filter
    get() = getStringOrNull(FitsKeyword.FILTER)?.ifBlank { null }?.trim()

inline val ReadableHeader.frame
    get() = (getStringOrNull("FRAME") ?: getStringOrNull(FitsKeyword.IMAGETYP))?.ifBlank { null }?.trim()

inline val ReadableHeader.instrument
    get() = getStringOrNull(FitsKeyword.INSTRUME)?.ifBlank { null }?.trim()

inline fun SeekableSource.fits() = Fits().also { it.read(this) }

inline fun String.fits() = FitsPath(this).also(FitsPath::read)

inline fun Path.fits() = FitsPath(this).also(FitsPath::read)

inline fun File.fits() = FitsPath(this).also(FitsPath::read)
