package nebulosa.time

import java.io.InputStream

data class IERSAB(
    private val iersa: IERSA,
    private val iersb: IERSB,
) : IERS() {

    override val columns: List<Column> = iersa.columns + iersb.columns

    override val time: DoubleArray
    override val pmX: DoubleArray
    override val pmY: DoubleArray
    override val dut1: DoubleArray

    init {
        val iers = HashMap<Int, Pair<Int, IERS>>()

        // Combine A and B columns, using B where possible.

        repeat(iersa.size) {
            iers[iersa.time(it).toInt()] = it to iersa
        }
        repeat(iersb.size) {
            iers[iersb.time(it).toInt()] = it to iersb
        }

        val mjds = iers.keys.toList().sorted()

        time = DoubleArray(mjds.size) { i -> iers[mjds[i]]!!.let { it.second.time(it.first) } }
        pmX = DoubleArray(mjds.size) { i -> iers[mjds[i]]!!.let { it.second.pmX(it.first) } }
        pmY = DoubleArray(mjds.size) { i -> iers[mjds[i]]!!.let { it.second.pmY(it.first) } }
        dut1 = DoubleArray(mjds.size) { i -> iers[mjds[i]]!!.let { it.second.dut1(it.first) } }
    }

    override fun canUseThisLine(line: String) = false

    override fun load(source: InputStream) = Unit

    override fun get(index: Int, column: Column): String {
        if (column is IERSA.Column) return iersa[index, column]
        return iersb[index, column]
    }
}
