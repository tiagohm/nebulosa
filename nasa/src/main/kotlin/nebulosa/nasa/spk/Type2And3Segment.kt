package nebulosa.nasa.spk

import nebulosa.constants.AU_KM
import nebulosa.constants.DAYSEC
import nebulosa.constants.J2000
import nebulosa.math.Vector3D
import nebulosa.math.divmod
import nebulosa.time.InstantOfTime
import java.io.IOException

/**
 * Chebyshev polynomial coefficients for the position (Type 2 and 3) and velocity (Type 3)
 * of the body as a function of time.
 */
internal data class Type2And3Segment(
    override val spk: Spk,
    override val source: String,
    override val start: Double,
    override val end: Double,
    override val center: Int,
    override val target: Int,
    override val frame: Int,
    override val type: Int,
    override val startIndex: Int,
    override val endIndex: Int,
) : SpkSegment {

    private class Coefficient(
        val mid: Double,
        val radius: Double,
        val x: DoubleArray,
        val y: DoubleArray,
        val z: DoubleArray,
        val count: Int,
    )

    private val initialEpoch: Double
    private val intervalLength: Double
    private val rsize: Int
    private val n: Int
    private val coefficients: MutableMap<Int, Coefficient>

    init {
        // INIT: is the initial epoch of the first record, given in ephemeris seconds past J2000.
        // INTLEN: is the length of the interval covered by each record, in seconds.
        // RSIZE: is the total size of (number of array elements in) each record.
        // N: is the number of records contained in the segment.
        val (a, b, c, d) = spk.daf.read(endIndex - 3, endIndex)
        initialEpoch = a
        intervalLength = b
        rsize = c.toInt()
        n = d.toInt()
        coefficients = HashMap(n)
    }

    private fun computeCoefficient(index: Int): Boolean {
        if (index in coefficients) return true

        val componentCount = (type - 1) * 3
        val coefficientCount = (rsize - 2) / componentCount
        val a = startIndex + index * rsize
        val b = a + rsize - 1

        return if (a in startIndex until b && b <= endIndex - 4) {
            val coefficients = spk.daf.read(a, b)

            val mid = coefficients[0]
            val radius = coefficients[1]
            val x = DoubleArray(coefficientCount)
            val y = DoubleArray(coefficientCount)
            val z = DoubleArray(coefficientCount)

            for (k in 0 until coefficientCount) {
                x[k] = coefficients[2 + k]
                y[k] = coefficients[2 + k + coefficientCount]
                z[k] = coefficients[2 + k + 2 * coefficientCount]
            }

            this.coefficients[index] = Coefficient(mid, radius, x, y, z, coefficientCount)

            true
        } else {
            false
        }
    }

    override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
        val (a, p) = ((time.tdb.whole - J2000) * DAYSEC - initialEpoch) divmod intervalLength
        val (b, q) = (time.tdb.fraction * DAYSEC) divmod intervalLength
        val (d, offset) = (p + q) divmod intervalLength
        val index = (a + b + d).toInt()

        // val t = (time.tdb.whole - J2000 + time.tdb.fraction) * DAYSEC
        // val index = ((t - initialEpoch) / intervalLength).toInt()

        if (!computeCoefficient(index)) {
            throw IOException("cannot find a segment that covers the date: ${time.value}")
        }

        // Chebyshev polynomial & differentiation. 158904.183237831
        val c = coefficients[index]!!

        // val s = 2.0 * (t - (c.mid - c.radius)) / intervalLength - 1.0
        val s = 2.0 * offset / intervalLength - 1.0
        val ss = 2.0 * s

        val w0 = DoubleArray(3)
        val w1 = DoubleArray(3)
        val w2 = DoubleArray(3)
        val dw0 = DoubleArray(3)
        val dw1 = DoubleArray(3)
        val dw2 = DoubleArray(3)

        for (i in c.count - 1 downTo 1) {
            // Polynomial.

            w2[0] = w1[0]
            w2[1] = w1[1]
            w2[2] = w1[2]

            w1[0] = w0[0]
            w1[1] = w0[1]
            w1[2] = w0[2]

            w0[0] = c.x[i] + (ss * w1[0] - w2[0])
            w0[1] = c.y[i] + (ss * w1[1] - w2[1])
            w0[2] = c.z[i] + (ss * w1[2] - w2[2])

            // Differentiation.

            dw2[0] = dw1[0]
            dw2[1] = dw1[1]
            dw2[2] = dw1[2]

            dw1[0] = dw0[0]
            dw1[1] = dw0[1]
            dw1[2] = dw0[2]

            dw0[0] = 2.0 * w1[0] + dw1[0] * ss - dw2[0]
            dw0[1] = 2.0 * w1[1] + dw1[1] * ss - dw2[1]
            dw0[2] = 2.0 * w1[2] + dw1[2] * ss - dw2[2]
        }

        // au
        val c0 = (c.x[0] + (s * w0[0] - w1[0])) / AU_KM
        val c1 = (c.y[0] + (s * w0[1] - w1[1])) / AU_KM
        val c2 = (c.z[0] + (s * w0[2] - w1[2])) / AU_KM

        // au/day
        val r0 = ((w0[0] + s * dw0[0] - dw1[0]) / intervalLength) * 2.0 * DAYSEC / AU_KM
        val r1 = ((w0[1] + s * dw0[1] - dw1[1]) / intervalLength) * 2.0 * DAYSEC / AU_KM
        val r2 = ((w0[2] + s * dw0[2] - dw1[2]) / intervalLength) * 2.0 * DAYSEC / AU_KM

        return Vector3D(c0, c1, c2) to Vector3D(r0, r1, r2)
    }
}
