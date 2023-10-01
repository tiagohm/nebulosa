package nebulosa.time

import nebulosa.math.*
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

abstract class IERS : PolarMotion, DeltaTime, Collection<List<String>> {

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
        val value = if (time is UT1 || time is UTC) time.value else time.tt.value
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

    override fun delta(time: InstantOfTime): Double {
        val dt = interpolate(time, this.time, dut1)[0]
        return if (dt.isNaN()) DeltaTime.Standard.delta(time)
        else dt
    }

    companion object : PolarMotion, DeltaTime {

        @Volatile private var polarMotion: PolarMotion = PolarMotion.None
        @Volatile private var deltaTime: DeltaTime = DeltaTime.Standard

        fun attach(iers: IERS) {
            polarMotion = iers
            deltaTime = iers
        }

        fun detach() {
            polarMotion = PolarMotion.None
            deltaTime = DeltaTime.Standard
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
            return deltaTime.delta(time)
        }
    }
}
