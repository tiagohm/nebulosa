package nebulosa.time

import nebulosa.erfa.eraUt1Utc
import nebulosa.math.*
import java.io.InputStream
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

    protected abstract val columns: List<Column>

    protected abstract val time: DoubleArray

    protected abstract val pmX: DoubleArray

    protected abstract val pmY: DoubleArray

    protected abstract val dut1: DoubleArray

    protected abstract fun canUseThisLine(line: String): Boolean

    operator fun get(index: Int, column: Column) = data[index][column.ordinal]

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
        val i = input.search(value, rightSide = true)
        val k = max(1, min(i, input.size - 1))
        val t0 = input[k - 1]
        val t1 = input[k]

        return DoubleArray(data.size) {
            if (i <= 0) Double.NaN
            else if (i >= input.size) Double.NaN
            else {
                val a = data[it][k - 1]
                val b = data[it][k]
                a + (b - a) / (t1 - t0) * (value - t0)
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

    override fun pmXY(time: InstantOfTime): PairOfAngle {
        val xy = interpolate(time, this.time, pmX, pmY)
        if (xy[0].isNaN() && xy[1].isNaN()) return PairOfAngle.ZERO
        val x = if (xy[0].isNaN()) 0.0 else xy[0].arcsec
        val y = if (xy[1].isNaN()) 0.0 else xy[1].arcsec
        return PairOfAngle(x, y)
    }

    /**
     * Computes UT1 - UTC in seconds at [time].
     */
    override fun delta(time: InstantOfTime): Double {
        return interpolate(time, this.time, dut1)[0].takeIf(Double::isFinite) ?: eraUt1Utc(time.whole, time.fraction, 0.0)[1]
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

        override fun pmXY(time: InstantOfTime): PairOfAngle {
            return polarMotion.pmXY(time)
        }

        override fun pmAngles(time: InstantOfTime): TripleOfAngle {
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
