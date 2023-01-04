@file:JvmName("MathAlgorithms")

package nebulosa.math

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
    left: Double = Double.NaN,
    right: Double = Double.NaN,
): Double {
    if (x <= a[0]) return if (left.isNaN()) b[0] else left
    if (x >= a[a.size - 1]) return if (right.isNaN()) b[a.size - 1] else right
    val i = a.binarySearch(x).let { if (it < 0) -it - 1 else it }
    return b[i - 1] + ((b[i] - b[i - 1]) / (a[i] - a[i - 1])) * (x - a[i - 1])
}

/**
 * Computes interpolation of [x] between [a] and array of indexes from 0 until [a].size.
 */
fun interpolation(
    x: Double,
    a: DoubleArray,
    left: Double = Double.NaN,
    right: Double = Double.NaN,
): Double {
    if (x <= a[0]) return if (left.isNaN()) 0.0 else left
    if (x >= a[a.size - 1]) return if (right.isNaN()) (a.size - 1).toDouble() else right
    val i = a.binarySearch(x).let { if (it < 0) -it - 1 else it }
    return (i - 1) + (x - a[i - 1]) / (a[i] - a[i - 1])
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
fun map(x: Int, inMin: Int, inMax: Int, outMin: Int, outMax: Int): Int {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

/**
 * Remaps a number [x] from one range to another.
 */
fun map(x: Long, inMin: Long, inMax: Long, outMin: Long, outMax: Long): Long {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

/**
 * Remaps a number [x] from one range to another.
 */
fun map(x: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}

/**
 * Remaps a number [x] from one range to another.
 */
fun map(x: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double {
    return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
}
