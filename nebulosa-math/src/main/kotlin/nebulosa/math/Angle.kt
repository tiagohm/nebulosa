@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package nebulosa.math

import nebulosa.constants.AMIN2RAD
import nebulosa.constants.ASEC2RAD
import nebulosa.constants.DEG2RAD
import nebulosa.constants.MILLIASEC2RAD
import nebulosa.constants.PI
import nebulosa.constants.PIOVERTWO
import nebulosa.constants.RAD2DEG
import nebulosa.constants.TAU
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Represents a angle value in radians.
 */
typealias Angle = Double

const val SEMICIRCLE: Angle = PI
const val CIRCLE: Angle = TAU
const val QUARTER: Angle = PIOVERTWO

inline val Angle.toDegrees
    get() = this * RAD2DEG

inline val Angle.toHours
    get() = this * (12.0 / PI)

inline val Angle.toArcmin
    get() = this * (60.0 * 180.0 / PI)

inline val Angle.toArcsec
    get() = this * (3600.0 * 180.0 / PI)

inline val Angle.toMas
    get() = this * (3600000.0 * 180.0 / PI)

inline fun Radians(value: Double): Angle = value

inline fun Degrees(value: Double) = value.rad

inline fun ArcMin(value: Double) = value.arcmin

inline fun ArcSec(value: Double) = value.arcsec

inline fun MilliArcSec(value: Double) = value.mas

inline val Double.rad
    get() = Radians(this)

inline val Float.rad
    get() = toDouble().rad

inline val Int.rad
    get() = toDouble().rad

inline val Double.mas
    get() = (this * MILLIASEC2RAD).rad

inline val Int.mas
    get() = toDouble().mas

inline val Double.arcsec
    get() = (this * ASEC2RAD).rad

inline val Int.arcsec
    get() = toDouble().arcsec

inline val Double.arcmin
    get() = (this * AMIN2RAD).rad

inline val Int.arcmin
    get() = toDouble().arcmin

inline val Double.deg
    get() = (this * DEG2RAD).rad

inline val Int.deg
    get() = toDouble().deg

inline val Double.hours
    get() = (this * PI / 12.0).rad

inline val Int.hours
    get() = toDouble().hours

inline val Angle.normalized
    get() = (this pmod TAU).rad

inline val Angle.cos
    get() = cos(this)

inline val Angle.sin
    get() = sin(this)

inline val Angle.tan
    get() = tan(this)

fun Angle.hms(): DoubleArray {
    val hours = normalized.toHours
    val minutes = (hours - hours.toInt()) * 60.0 % 60.0
    val seconds = (minutes - minutes.toInt()) * 60.0 % 60.0
    return doubleArrayOf(hours, minutes, seconds)
}

fun Angle.dms(): DoubleArray {
    val degrees = abs(toDegrees)
    val minutes = (degrees - degrees.toInt()) * 60.0 % 60.0
    val seconds = (minutes - minutes.toInt()) * 60.0 % 60.0
    return doubleArrayOf(if (this < 0.0) -degrees else degrees, minutes, seconds)
}

inline fun Angle.format(formatter: AngleFormatter) = formatter.format(this)

inline fun Angle.formatHMS() = format(AngleFormatter.HMS)

inline fun Angle.formatDMS() = format(AngleFormatter.DMS)

inline fun Angle.formatSignedDMS() = format(AngleFormatter.SIGNED_DMS)

inline fun HMS(hour: Int, minute: Int, second: Double = 0.0) = (hour + minute / 60.0 + second / 3600.0).hours

inline fun DMS(degrees: Int, minute: Int, second: Double = 0.0, negative: Boolean = degrees < 0) =
    (abs(degrees) + minute / 60.0 + second / 3600.0).let { if (negative) -it else it }.deg

private val PARSE_COORDINATES_NOT_NUMBER_REGEX = Regex("[^\\-\\d.]+")
private val UNICODE_SIGN_MINUS_REGEX = Regex("−+")

fun Angle(
    input: String?,
    isHours: Boolean = false,
    decimalIsHours: Boolean = isHours,
    defaultValue: Angle = Double.NaN,
): Angle {
    val trimmedInput = input?.trim() ?: return defaultValue

    val decimalInput = trimmedInput.toDoubleOrNull()
    if (decimalInput != null) return if (decimalIsHours) decimalInput.hours
    else decimalInput.deg

    val tokenizer = StringTokenizer(trimmedInput, " \t\n\rhms°'\":*#")
    val res = DoubleArray(3)
    var idx = 0
    var sign = 1.0

    while (idx < 3 && tokenizer.hasMoreElements()) {
        val token = tokenizer.nextToken()
            .replace(UNICODE_SIGN_MINUS_REGEX, "-")
            .replace(PARSE_COORDINATES_NOT_NUMBER_REGEX, "").trim()

        if (token.isEmpty()) continue

        if (idx == 0 && token == "-") {
            sign = -1.0
            continue
        }

        val value = token.toDoubleOrNull() ?: continue

        if (idx == 0 && value < 0.0) {
            sign = -1.0
        }

        res[idx++] = abs(value)
    }

    if (idx == 0) return defaultValue

    if (res[2] >= 60.0) {
        res[2] %= 60.0
        res[1] += 1.0
    }

    if (res[1] >= 60.0) {
        res[1] %= 60.0
        res[0] += 1.0
    }

    val value = sign * (res[0] + res[1] / 60.0 + res[2] / 3600.0)

    return if (isHours) value.hours else value.deg
}

inline val String?.hours
    get() = Angle(this, true)

inline val String?.deg
    get() = Angle(this)
