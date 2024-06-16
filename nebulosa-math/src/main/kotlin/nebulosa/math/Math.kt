@file:JvmName("Math")
@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.math

import kotlin.math.floor

/**
 * Adds [a] and [b] exactly, returning the result as two 64-bit floats.
 *
 * Uses the procedure of Shewchuk, 1997,
 * Discrete & Computational Geometry 18(3):305-363
 *
 * @see <a href="http://www.cs.berkeley.edu/~jrs/papers/robustr.pdf">Paper</a>
 */
fun twoSum(a: Double, b: Double): DoubleArray {
    val x = a + b
    var eb = x - a
    var ea = x - eb
    eb = b - eb
    ea = a - ea
    return doubleArrayOf(x, ea + eb)
}

/**
 * Multiples [a] and [b] exactly, returning the result as two 64-bit floats.
 * The first is the approximate product (with some floating point error)
 * and the second is the error of the float64 product.
 *
 * Uses the procedure of Shewchuk, 1997,
 * Discrete & Computational Geometry 18(3):305-363
 *
 * @see <a href="http://www.cs.berkeley.edu/~jrs/papers/robustr.pdf">Paper</a>
 */
fun twoProduct(a: Double, b: Double): DoubleArray {
    val x = a * b
    val (ah, al) = split(a)
    val (bh, bl) = split(b)
    val y1 = ah * bh
    var y = x - y1
    val y2 = al * bh
    y -= y2
    val y3 = ah * bl
    y -= y3
    val y4 = al * bl
    y = y4 - y
    return doubleArrayOf(x, y)
}

/**
 * Splits 64-bit float in two aligned parts.
 *
 * Uses the procedure of Shewchuk, 1997,
 * Discrete & Computational Geometry 18(3):305-363
 *
 * @see <a href="http://www.cs.berkeley.edu/~jrs/papers/robustr.pdf">Paper</a>
 */
fun split(a: Double): DoubleArray {
    val c = 134217729.0 * a
    val abig = c - a
    val ah = c - abig
    val al = a - ah
    return doubleArrayOf(ah, al)
}

/**
 * Computes interpolation of [x] between the arrays [a] and [b].
 */
fun interpolation(
    x: Double,
    a: DoubleArray,
    b: DoubleArray,
    left: Double = b[0],
    right: Double = b[a.size - 1],
): Double {
    if (x <= a[0]) return left
    if (x >= a[a.size - 1]) return right
    val i = a.binarySearch(x).let { if (it < 0) -it - 1 else it }
    return b[i - 1] + (b[i] - b[i - 1]) / (a[i] - a[i - 1]) * (x - a[i - 1])
}

/**
 * Computes interpolation of [x] between [a] and array of indexes from 0 until [a].size.
 */
fun interpolation(
    x: Double,
    a: DoubleArray,
    left: Double = 0.0,
    right: Double = (a.size - 1).toDouble(),
): Double {
    if (x <= a[0]) return left
    if (x >= a[a.size - 1]) return right
    val i = a.binarySearch(x).let { if (it < 0) -it - 1 else it }
    return i - 1 + (x - a[i - 1]) / (a[i] - a[i - 1])
}

fun DoubleArray.search(
    element: Double,
    rightSide: Boolean = false,
    fromIndex: Int = 0,
    toIndex: Int = size,
): Int {
    val idx = binarySearch(element, fromIndex, toIndex)
    return if (idx < 0) -idx - 1 else if (rightSide) idx + 1 else idx
}

/**
 * Remaps a number [x] from one range to another.
 */
inline fun map(x: Int, inMin: Int, inMax: Int, outMin: Int, outMax: Int): Int {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

/**
 * Remaps a number [x] from one range to another.
 */
inline fun map(x: Long, inMin: Long, inMax: Long, outMin: Long, outMax: Long): Long {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

/**
 * Remaps a number [x] from one range to another.
 */
inline fun map(x: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

/**
 * Remaps a number [x] from one range to another.
 */
inline fun map(x: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

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

/**
 * Computes the square number.
 */
inline val Double.squared
    get() = this * this

/**
 * Computes the cube number.
 */
inline val Double.cubic
    get() = this * this * this
