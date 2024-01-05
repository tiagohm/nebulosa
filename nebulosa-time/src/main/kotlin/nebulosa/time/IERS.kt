package nebulosa.time

import nebulosa.constants.MJD0
import nebulosa.erfa.eraUt1Utc
import nebulosa.math.Matrix3D
import nebulosa.math.arcsec
import nebulosa.math.search
import java.io.InputStream
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

// https://hpiers.obspm.fr/iers/bul/bulb/explanatory.pdf

abstract class IERS : PolarMotion, TimeDelta, Collection<List<String>> {

    interface Column {

        val ordinal: Int

        val byteStart: Int

        val byteEnd: Int
    }

    protected val data = ArrayList<List<String>>()

    abstract val columns: List<Column>

    protected abstract val time: DoubleArray

    protected abstract val pmX: DoubleArray

    protected abstract val pmY: DoubleArray

    protected abstract val dut1: DoubleArray

    protected abstract fun canUseThisLine(line: String): Boolean

    open operator fun get(index: Int, column: Column) = data[index][column.ordinal]

    fun time(index: Int) = time[index]

    fun pmX(index: Int) = pmX[index]

    fun pmY(index: Int) = pmY[index]

    fun dut1(index: Int) = dut1[index]

    override val size
        get() = data.size

    override fun contains(element: List<String>) = element in data

    override fun containsAll(elements: Collection<List<String>>) = data.containsAll(elements)

    override fun isEmpty() = data.isEmpty()

    override fun iterator() = data.iterator()

    protected fun interpolate(
        time: InstantOfTime,
        input: DoubleArray,
        vararg data: DoubleArray,
    ): DoubleArray {
        val value = time.value
        val mjd = floor(time.whole - MJD0 + time.fraction)
        val utc = time.whole - (MJD0 + mjd) + time.fraction

        val i = input.search(mjd, rightSide = true)
        val k = max(1, min(i, input.size - 1))
        val t0 = input[k - 1]
        val t1 = input[k]

        return DoubleArray(data.size) {
            // Do not extrapolate outside range, instead just propagate last values.
            if (i <= 0) data[it].first()
            else if (i >= input.size) data[it].last()
            else {
                val a = data[it][k - 1]
                val b = data[it][k]
                a + (b - a) / (t1 - t0) * (value - t0)
                a + (mjd - t0 + utc) / (t1 - t0) * (b - a)
            }
        }
    }

    @Synchronized
    open fun load(source: InputStream) {
        data.clear()

        source.use {
            for (line in source.bufferedReader().lines()) {
                if (canUseThisLine(line)) {
                    data.add(columns.map { line.substring(it.byteStart, it.byteEnd + 1).trim() })
                }
            }
        }
    }

    override fun pmXY(time: InstantOfTime): DoubleArray {
        val xy = interpolate(time, this.time, pmX, pmY)
        if (xy[0].isNaN() && xy[1].isNaN()) return doubleArrayOf(0.0, 0.0)
        val x = if (xy[0].isNaN()) 0.0 else xy[0].arcsec
        val y = if (xy[1].isNaN()) 0.0 else xy[1].arcsec
        return doubleArrayOf(x, y)
    }

    /**
     * Computes UT1 - UTC in seconds at [time].
     */
    override fun delta(time: InstantOfTime): Double {
        return interpolate(time, this.time, dut1)[0]
    }

    companion object : PolarMotion, TimeDelta {

        @Volatile private var polarMotion: PolarMotion = PolarMotion.None
        @Volatile private var timeDelta: TimeDelta? = null

        fun attach(iers: IERS) {
            polarMotion = iers
            timeDelta = iers
        }

        fun detach() {
            polarMotion = PolarMotion.None
            timeDelta = null
        }

        override fun pmXY(time: InstantOfTime): DoubleArray {
            return polarMotion.pmXY(time)
        }

        override fun pmAngles(time: InstantOfTime): DoubleArray {
            return polarMotion.pmAngles(time)
        }

        override fun pmMatrix(time: InstantOfTime): Matrix3D {
            return polarMotion.pmMatrix(time)
        }

        override fun delta(time: InstantOfTime): Double {
            return timeDelta?.delta(time) ?: eraUt1Utc(time.whole, time.fraction, 0.0)[1]
        }
    }
}
