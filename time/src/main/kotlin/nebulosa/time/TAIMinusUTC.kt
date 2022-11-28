package nebulosa.time

import nebulosa.constants.MJD0

object TAIMinusUTC : DeltaTime {

    override fun delta(time: InstantOfTime): Double {
        val (year, month) = time.asYearMonthDayAndFraction()

        if (year < CHANGES[0][0]) return 0.0

        val m = 12 * year + month
        var k = -1

        for (i in CHANGES.indices.reversed()) {
            if (m >= (12 * CHANGES[i][0] + CHANGES[i][1])) {
                k = i
                break
            }
        }

        if (k < 0) return 0.0

        val da = CHANGES[k][2]

        // If pre-1972, adjust for drift.
        return if (k < 14) {
            da + (time.whole - MJD0 + time.fraction - DRIFT[k][0]) * DRIFT[k][1]
        } else {
            da
        }
    }

    // Reference dates (MJD) and drift rates (s/day), pre leap seconds.
    private val DRIFT = arrayOf(
        doubleArrayOf(37300.0, 0.0012960),
        doubleArrayOf(37300.0, 0.0012960),
        doubleArrayOf(37300.0, 0.0012960),
        doubleArrayOf(37665.0, 0.0011232),
        doubleArrayOf(37665.0, 0.0011232),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(38761.0, 0.0012960),
        doubleArrayOf(39126.0, 0.0025920),
        doubleArrayOf(39126.0, 0.0025920),
    )

    // Dates and Delta(AT)s.
    private val CHANGES = arrayOf(
        doubleArrayOf(1960.0, 1.0, 1.4178180),
        doubleArrayOf(1961.0, 1.0, 1.4228180),
        doubleArrayOf(1961.0, 8.0, 1.3728180),
        doubleArrayOf(1962.0, 1.0, 1.8458580),
        doubleArrayOf(1963.0, 11.0, 1.9458580),
        doubleArrayOf(1964.0, 1.0, 3.2401300),
        doubleArrayOf(1964.0, 4.0, 3.3401300),
        doubleArrayOf(1964.0, 9.0, 3.4401300),
        doubleArrayOf(1965.0, 1.0, 3.5401300),
        doubleArrayOf(1965.0, 3.0, 3.6401300),
        doubleArrayOf(1965.0, 7.0, 3.7401300),
        doubleArrayOf(1965.0, 9.0, 3.8401300),
        doubleArrayOf(1966.0, 1.0, 4.3131700),
        doubleArrayOf(1968.0, 2.0, 4.2131700),
        doubleArrayOf(1972.0, 1.0, 10.0),
        doubleArrayOf(1972.0, 7.0, 11.0),
        doubleArrayOf(1973.0, 1.0, 12.0),
        doubleArrayOf(1974.0, 1.0, 13.0),
        doubleArrayOf(1975.0, 1.0, 14.0),
        doubleArrayOf(1976.0, 1.0, 15.0),
        doubleArrayOf(1977.0, 1.0, 16.0),
        doubleArrayOf(1978.0, 1.0, 17.0),
        doubleArrayOf(1979.0, 1.0, 18.0),
        doubleArrayOf(1980.0, 1.0, 19.0),
        doubleArrayOf(1981.0, 7.0, 20.0),
        doubleArrayOf(1982.0, 7.0, 21.0),
        doubleArrayOf(1983.0, 7.0, 22.0),
        doubleArrayOf(1985.0, 7.0, 23.0),
        doubleArrayOf(1988.0, 1.0, 24.0),
        doubleArrayOf(1990.0, 1.0, 25.0),
        doubleArrayOf(1991.0, 1.0, 26.0),
        doubleArrayOf(1992.0, 7.0, 27.0),
        doubleArrayOf(1993.0, 7.0, 28.0),
        doubleArrayOf(1994.0, 7.0, 29.0),
        doubleArrayOf(1996.0, 1.0, 30.0),
        doubleArrayOf(1997.0, 7.0, 31.0),
        doubleArrayOf(1999.0, 1.0, 32.0),
        doubleArrayOf(2006.0, 1.0, 33.0),
        doubleArrayOf(2009.0, 1.0, 34.0),
        doubleArrayOf(2012.0, 7.0, 35.0),
        doubleArrayOf(2015.0, 7.0, 36.0),
        doubleArrayOf(2017.0, 1.0, 37.0),
    )
}
