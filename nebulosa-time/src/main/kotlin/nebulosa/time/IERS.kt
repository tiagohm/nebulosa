package nebulosa.time

import nebulosa.constants.MJD0
import nebulosa.erfa.PairOfAngle
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.search
import java.io.InputStream
import kotlin.math.floor
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

    internal abstract val mjd: DoubleArray

    internal abstract val pmX: DoubleArray

    internal abstract val pmY: DoubleArray

    internal abstract val dut1: DoubleArray

    protected abstract fun canUseThisLine(line: String): Boolean

    operator fun get(index: Int, column: Column) = data[index][column.ordinal]

    override val size get() = data.size

    override fun contains(element: List<String>) = element in data

    override fun containsAll(elements: Collection<List<String>>) = data.containsAll(elements)

    override fun isEmpty() = data.isEmpty()

    override fun iterator() = data.iterator()

    protected fun interpolate(
        time: InstantOfTime,
        input: DoubleArray,
        vararg data: DoubleArray,
    ): DoubleArray {
        val jd = if (time is TimeJD) time else time.utc
        val res = DoubleArray(data.size)
        val mjd = floor(jd.whole - MJD0 + jd.fraction)
        val utc = jd.whole - (MJD0 + mjd) + jd.fraction
        val i = input.search(mjd, rightSide = true)
        val k = max(1, min(i, input.size - 1))
        val mjd0 = input[k - 1]
        val mjd1 = input[k]

        for (j in res.indices) {
            res[j] = if (i == 0) data[j].first()
            else if (i >= input.size) data[j].last()
            else {
                val a = data[j][k - 1]
                val b = data[j][k]
                val c = b - a
                a + (mjd - mjd0 + utc) / (mjd1 - mjd0) * c
            }
        }

        return res
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

    // TODO: CACHE?
    override fun pmXY(time: InstantOfTime): PairOfAngle {
        val (x, y) = interpolate(time, mjd, pmX, pmY)
        return PairOfAngle(x.arcsec, y.arcsec)
    }

    // TODO: CACHE?
    override fun delta(time: InstantOfTime): Double {
        return interpolate(time, mjd, dut1)[0]
    }

    companion object : PolarMotion, DeltaTime {

        // TODO: Initialize with default IERS (empty?)
        @Volatile lateinit var current: IERS

        override fun pmXY(time: InstantOfTime) = current.pmXY(time)

        override fun delta(time: InstantOfTime) = current.delta(time)
    }
}
