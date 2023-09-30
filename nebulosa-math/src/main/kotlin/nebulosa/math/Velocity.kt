@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.DAYSEC

/**
 * Represents a velocity value in au/day.
 */
typealias Velocity = Double

inline val Velocity.toKilometersPerSecond
    get() = this * AU_KM / DAYSEC

inline val Velocity.toMetersPerSecond
    get() = this * AU_M / DAYSEC

inline fun AUPerDay(value: Double): Velocity = value

inline fun KilometerPerSecond(value: Double) = value.kms

inline fun MeterPerSecond(value: Double) = value.ms

inline val Double.auDay
    get() = AUPerDay(this)

inline val Int.auDay
    get() = toDouble().auDay

inline val Double.kms
    get() = (this * DAYSEC / AU_KM).auDay

inline val Int.kms
    get() = (this * DAYSEC / AU_KM).auDay

inline val Double.ms
    get() = (this * DAYSEC / AU_M).auDay

inline val Int.ms
    get() = (this * DAYSEC / AU_M).auDay
