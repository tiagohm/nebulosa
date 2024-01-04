package nebulosa.time

import java.io.InputStream

/**
 * @see <a href="https://hpiers.obspm.fr/iers/eop/eopc04/eopc04.1962-now">Table</a>
 */
class IERSB : IERS() {

    override val columns: List<Column> = Column.entries

    override lateinit var time: DoubleArray

    override lateinit var pmX: DoubleArray

    override lateinit var pmY: DoubleArray

    override lateinit var dut1: DoubleArray

    override fun canUseThisLine(line: String) = !line.startsWith("#") && line.isNotBlank()

    override fun load(source: InputStream) {
        super.load(source)

        time = DoubleArray(size) { this[it, Column.MJD].toDouble() }
        pmX = DoubleArray(size) { this[it, Column.PM_X].toDouble() }
        pmY = DoubleArray(size) { this[it, Column.PM_Y].toDouble() }
        dut1 = DoubleArray(size) { this[it, Column.DUT1].toDouble() }
    }

    /**
     * @see <a href="https://hpiers.obspm.fr/eoppc/eop/eopc04/eopc04.txt">Description</a>
     */
    enum class Column(
        override val byteStart: Int,
        override val byteEnd: Int,
    ) : IERS.Column {
        YEAR(0, 3),
        MONTH(4, 7),
        DAY(8, 11),
        MJD(16, 25),
        PM_X(26, 37),
        PM_Y(38, 49),
        DUT1(50, 61),
    }

    companion object {

        const val URL = "https://hpiers.obspm.fr/iers/eop/eopc04/eopc04.1962-now"
    }
}
