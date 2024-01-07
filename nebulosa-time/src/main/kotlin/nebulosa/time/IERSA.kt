package nebulosa.time

import java.io.InputStream

/**
 * @see <a href="https://maia.usno.navy.mil/ser7/finals2000A.all">Table</a>
 */
class IERSA : IERS() {

    /**
     * @see <a href="https://maia.usno.navy.mil/ser7/readme.finals2000A">Reference</a>
     */
    enum class Column(
        override val byteStart: Int,
        override val byteEnd: Int,
    ) : IERS.Column {
        YEAR(0, 1), // year (to get true calendar year, add 1900 for MJD<=51543 or add 2000 for MJD>=51544)
        MONTH(2, 3), // month number
        DAY(4, 5), // day of month
        MJD(7, 14), // fractional Modified Julian Date (MJD UTC)
        PM_A_FLAG(16, 16), // IERS (I) or Prediction (P) flag for Bull. A polar motion values
        PM_X_A(18, 26), // Bull. A PM-x (sec. of arc)
        PM_X_A_ERR(27, 35), // error in PM-x (sec. of arc)
        PM_Y_A(37, 45), // Bull. A PM-y (sec. of arc)
        PM_Y_A_ERR(46, 54), // error in PM-y (sec. of arc)
        DUT1_A_FLAG(57, 57), // IERS (I) or Prediction (P) flag for Bull. A UT1-UTC values
        DUT1_A(58, 67), // Bull. A UT1-UTC (sec. of time)
        DUT1_A_ERR(68, 77), // error in UT1-UTC (sec. of time)
        LOD_A(79, 85), // Bull. A LOD (msec. of time) -- NOT ALWAYS FILLED
        LOD_A_ERR(86, 92), // error in LOD (msec. of time) -- NOT ALWAYS FILLED
        NUT_A_FLAG(95, 95), // IERS (I) or Prediction (P) flag for Bull. A nutation values
        DX_A_2000A(97, 105), // Bull. A dX wrt IAU2000A Nutation (msec. of arc), Free Core Nutation NOT Removed
        DX_A_2000A_ERR(106, 114), // error in dX (msec. of arc)
        DY_A_2000A(116, 124), // Bull. A dY wrt IAU2000A Nutation (msec. of arc), Free Core Nutation NOT Removed
        DY_A_2000A_ERR(125, 133), // error in dY (msec. of arc)
        PM_X_B(134, 143), // Bull. B PM-x (sec. of arc)
        PM_Y_B(144, 153), // Bull. B PM-y (sec. of arc)
        DUT1_B(154, 164), // Bull. B UT1-UTC (sec. of time)
        DX_B_2000A(165, 174), // Bull. B dX wrt IAU2000A Nutation (msec. of arc)
        DX_B_2000A_ERR(175, 184), // Bull. B dY wrt IAU2000A Nutation (msec. of arc)
    }

    override lateinit var time: DoubleArray
        private set

    override lateinit var pmX: DoubleArray
        private set

    override lateinit var pmY: DoubleArray
        private set

    override lateinit var dut1: DoubleArray
        private set

    override val columns: List<Column> = Column.entries

    override fun canUseThisLine(line: String) = line.trim().length > 17 && (line[16] == 'I' || line[16] == 'P')

    override fun load(source: InputStream) {
        super.load(source)

        time = DoubleArray(size) { this[it, Column.MJD].toDouble() }
        pmX = DoubleArray(size) { compute(Column.PM_X_A, Column.PM_X_B, it) }
        pmY = DoubleArray(size) { compute(Column.PM_Y_A, Column.PM_Y_B, it) }
        dut1 = DoubleArray(size) { compute(Column.DUT1_A, Column.DUT1_B, it) }
    }

    companion object {

        const val URL = "https://datacenter.iers.org/data/9/finals2000A.all"

        @JvmStatic
        private fun IERS.compute(a: IERS.Column, b: IERS.Column, index: Int): Double {
            return this[index, b].toDoubleOrNull() ?: this[index, a].toDoubleOrNull() ?: 0.0
        }
    }
}

