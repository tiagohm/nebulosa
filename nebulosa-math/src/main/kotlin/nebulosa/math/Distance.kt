@file:Suppress("FloatingPointLiteralPrecision", "NOTHING_TO_INLINE", "FunctionName")

package nebulosa.math

import nebulosa.constants.AU_KM
import nebulosa.constants.AU_M
import nebulosa.constants.SPEED_OF_LIGHT

/**
 * Represents a distance value in AU.
 */
typealias Distance = Double

const val ONE_PARSEC = 206264.806245480309552772371736702884 // AU
const val ONE_GIGAPARSEC = 1000000000.0 * ONE_PARSEC

inline val Distance.toMeters
    get() = this * AU_M

inline val Distance.toKilometers
    get() = this * AU_KM

inline val Distance.toLightYears
    get() = this / (SPEED_OF_LIGHT * 31557600 / AU_M)

inline val Distance.toParsecs
    get() = this / ONE_PARSEC

inline fun AU(value: Double): Distance = value

inline fun Meter(value: Double): Distance = value.m

inline fun LightYear(value: Double): Distance = value.ly

inline fun Parsec(value: Double): Distance = value.parsec

inline val Double.au
    get() = AU(this)

inline val Int.au
    get() = toDouble().au

inline val Double.m
    get() = (this / AU_M).au

inline val Int.m
    get() = toDouble().m

inline val Double.km
    get() = (this / AU_KM).au

inline val Int.km
    get() = toDouble().km

inline val Double.ly
    get() = (this * (SPEED_OF_LIGHT * 31557600 / AU_M)).au

inline val Int.ly
    get() = toDouble().ly

inline val Double.parsec
    get() = (this * ONE_PARSEC).au

inline val Int.parsec
    get() = toDouble().parsec
