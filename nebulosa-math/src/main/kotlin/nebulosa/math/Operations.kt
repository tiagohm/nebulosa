@file:JvmName("MathOperations")

package nebulosa.math

import kotlin.math.floor

/**
 * Returns a pair containing the quotient and the remainder when this number is divided by [other].
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.divmod(other: Double) = doubleArrayOf(floor(this / other), this pmod other)

/**
 * Returns a pair containing the quotient and the remainder when this number is divided by [other].
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.divmod(other: Int) = intArrayOf(this / other, this pmod other)

/**
 * Computes the modulo where the result is always positive.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.amod(other: Double) = (this % other).let { if (it <= 0.0) it + other else it }

/**
 * Computes the modulo where the result is always positive.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.amod(other: Int) = (this % other).let { if (it <= 0) it + other else it }

/**
 * Computes the modulo where the result is always non-negative.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.pmod(other: Double) = (this % other).let { if (it < 0.0) it + other else it }

/**
 * Computes the modulo where the result is always non-negative.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.pmod(other: Int) = (this % other).let { if (it < 0) it + other else it }

/**
 * Computes the floor modulo.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.fmod(other: Double) = (this % other + other) % other

/**
 * Computes the floor modulo.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.fmod(other: Int) = (this % other + other) % other
