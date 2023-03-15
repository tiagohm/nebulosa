package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.constants.MJD0
import java.io.InputStream

/**
 * @see <a href="https://maia.usno.navy.mil/ser7/finals2000A.all">Table</a>
 */
object IERSA : IERS() {

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

    override val columns = Column.values().toList()

    override fun canUseThisLine(line: String) = line.trim().length > 17 && line[16] == 'I'

    override fun load(source: InputStream) {
        super.load(source)

        time = DoubleArray(size) {
            val mjd = this[it, Column.MJD].toDouble()
            mjd + (TT_MINUS_UTC / DAYSEC + MJD0)
        }

        pmX = DoubleArray(size) { this[it, Column.PM_X_A].toDouble() }

        pmY = DoubleArray(size) { this[it, Column.PM_Y_A].toDouble() }

        dut1 = DoubleArray(size) { this[it, Column.DUT1_A].toDouble() }

        val bigJumps = IntArray(dut1.size) { if (it > 0 && dut1[it] - dut1[it - 1] > 0.9) 1 else 0 }

        for (i in dut1.indices) {
            if (i > 0) bigJumps[i] += bigJumps[i - 1]
            val k = bigJumps[i] + TT_MINUS_UTC
            dut1[i] = k - dut1[i]
        }
    }

    const val URL = "https://datacenter.iers.org/data/9/finals2000A.all"

    private const val TT_MINUS_UTC = 32.184 + 12.0
}

