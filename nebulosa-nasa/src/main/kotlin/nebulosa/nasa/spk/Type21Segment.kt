package nebulosa.nasa.spk

import nebulosa.constants.AU_KM
import nebulosa.constants.DAYSEC
import nebulosa.constants.J2000
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Vector3D
import nebulosa.time.InstantOfTime
import java.io.IOException

/**
 * Extended Modified Difference Arrays.
 *
 * @see <a href="https://github.com/whiskie14142/spktype21/blob/master/package/spktype21/spktype21.py">Python Module</a>
 * @see <a href="https://naif.jpl.nasa.gov/naif/toolkit_FORTRAN_PC_Linux_IFORT_64bit.html">Fortran Toolkit</a>
 * @see <a href="https://www.imcce.fr/inpop/calceph">CALCEPH</a>
 */
internal data class Type21Segment(
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
        val tl: Double,
        val g: DoubleArray,
        val refPos: DoubleArray,
        val refVel: DoubleArray,
        val dt: Array<DoubleArray>,
        val kqmax1: Int,
        val kq: IntArray,
    )

    private val maxdim: Int
    private val dlsize: Int
    private val n: Int
    private val epochTable: DoubleArray
    private val epochDirCount: Int
    private val epochDir: DoubleArray

    private val coefficients: MutableMap<Int, Coefficient>

    init {
        val (a, b) = spk.daf.read(endIndex - 1, endIndex)
        maxdim = a.toInt() // Difference line size.
        dlsize = 4 * maxdim + 11
        n = b.toInt() // The number of records in a segment.
        // Epochs for all records in this segment.
        epochTable = spk.daf.read(startIndex + n * dlsize, startIndex + n * dlsize + n - 1)
        epochDirCount = n / 100
        epochDir = if (epochDirCount > 0) spk.daf.read(endIndex - epochDirCount - 1, endIndex - 2) else DoubleArray(0)
        coefficients = HashMap(n)
    }

    private fun searchCoefficientIndex(seconds: Double): Int {
        val searchStartIndex: Int
        val searchLastIndex: Int

        if (epochDirCount > 0) {
            // TODO: Not tested!
            var subdir = 0

            while (subdir < epochDirCount && epochDir[subdir] < seconds) {
                subdir++
            }

            searchStartIndex = subdir * 100
            searchLastIndex = (subdir + 1) * 100
        } else {
            searchStartIndex = 0
            searchLastIndex = n
        }

        var recordIndex = -1

        // Search target epoch in epoch table.
        for (i in searchStartIndex until searchLastIndex) {
            if (i < epochTable.size && epochTable[i] >= seconds) {
                recordIndex = i
                break
            }
        }

        if (recordIndex == -1) {
            throw IOException("cannot find a segment that covers the date: $seconds")
        }

        return recordIndex
    }

    private fun computeCoefficient(recordIndex: Int): Boolean {
        if (recordIndex in coefficients) return true

        val mdaRecord = spk.daf.read(
            startIndex + recordIndex * dlsize,
            startIndex + (recordIndex + 1) * dlsize - 1,
        )

        // Reference epoch of record.
        val tl = mdaRecord[0]
        // Stepsize function vector.
        val g = mdaRecord.sliceArray(1..maxdim)

        // Reference position & velocity vector.
        val refPos = DoubleArray(3)
        val refVel = DoubleArray(3)

        refPos[0] = mdaRecord[maxdim + 1]
        refVel[0] = mdaRecord[maxdim + 2]

        refPos[1] = mdaRecord[maxdim + 3]
        refVel[1] = mdaRecord[maxdim + 4]

        refPos[2] = mdaRecord[maxdim + 5]
        refVel[2] = mdaRecord[maxdim + 6]

        // val dt = mdaRecord.sliceArray(maxdim + 7 until 4 * maxdim + 7)
        val dt = Array(maxdim) { DoubleArray(3) }

        for (p in 0 until maxdim) {
            for (k in 0..2) {
                dt[p][k] = mdaRecord[maxdim + 7 + k * maxdim + p]
            }
        }

        // Initializing the difference table.
        val kq = IntArray(3)
        val kqmax1 = mdaRecord[4 * maxdim + 7].toInt()
        kq[0] = mdaRecord[4 * maxdim + 8].toInt()
        kq[1] = mdaRecord[4 * maxdim + 9].toInt()
        kq[2] = mdaRecord[4 * maxdim + 10].toInt()

        coefficients[recordIndex] = Coefficient(tl, g, refPos, refVel, dt, kqmax1, kq)

        return true
    }

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val seconds = (time.tdb.whole - J2000 + time.tdb.fraction) * DAYSEC

        val index = searchCoefficientIndex(seconds)

        if (!computeCoefficient(index)) {
            throw IOException("cannot find a segment that covers the date: $seconds")
        }

        val c = coefficients[index]!!

        // Next we set up for the computation of the various differences.
        val delta = seconds - c.tl
        var tp = delta
        val mpq2 = c.kqmax1 - 2
        var ks = c.kqmax1 - 1

        // TP starts out as the delta t between the request time and the
        // difference line's reference epoch. We then change it from DELTA
        // by the components of the stepsize vector G.
        val fc = DoubleArray(MAXTRM)
        val wc = DoubleArray(MAXTRM - 1)
        val w = DoubleArray(MAXTRM + 3)

        fc[0] = 1.0

        for (j in 0 until mpq2) {
            fc[j + 1] = tp / c.g[j]
            wc[j] = delta / c.g[j]
            tp = delta + c.g[j]
        }

        // Collect KQMAX1 reciprocals.

        for (j in 0..c.kqmax1) {
            w[j] = 1.0 / (j + 1)
        }

        // Compute the W(K) terms needed for the position interpolation
        // (Note, it is assumed throughout this routine that KS, which
        // starts out as KQMAX1-1 (the maximum integration) is at least 2.

        var jx = 0

        while (ks >= 2) {
            jx++

            for (j in 0 until jx) {
                w[j + ks] = fc[j + 1] * w[j + ks - 1] - wc[j] * w[j + ks]
            }

            ks--
        }

        // Perform position interpolation: (Note that KS = 1 right now.
        // We don't know much more than that.)
        val state = DoubleArray(6)

        for (i in 0..2) {
            val kqq = c.kq[i]
            var sum = 0.0

            for (j in kqq - 1 downTo 0) {
                sum += c.dt[j][i] * w[j + ks]
            }

            state[i] = (c.refPos[i] + delta * (c.refVel[i] + delta * sum)) / AU_KM
        }

        // Again we need to compute the W(K) coefficients that are
        // going to be used in the velocity interpolation.
        // (Note, at this point, KS = 1, KS1 = 0.)

        for (j in 0 until jx) {
            w[j + ks] = fc[j + 1] * w[j + ks - 1] - wc[j] * w[j + ks]
        }

        ks--

        // Perform velocity interpolation.
        for (i in 0..2) {
            val kqq = c.kq[i]
            var sum = 0.0

            for (j in kqq - 1 downTo 0) {
                sum += c.dt[j][i] * w[j + ks]
            }

            state[i + 3] = (c.refVel[i] + delta * sum) * DAYSEC / AU_KM
        }

        return PositionAndVelocity(Vector3D(state), Vector3D(state, 3))
    }

    companion object {

        private const val MAXTRM = 25
    }
}
