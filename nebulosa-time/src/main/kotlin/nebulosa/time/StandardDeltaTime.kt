package nebulosa.time

import nebulosa.io.bufferedResource
import nebulosa.io.readDoubleArrayLe
import kotlin.math.min

object StandardDeltaTime : DeltaTime {

    private val DAILY_TT = bufferedResource("DAILY_TT.dat") { readDoubleArrayLe(17876) }
    private val DAILY_DELTA_T = bufferedResource("DAILY_DELTA_T.dat") { readDoubleArrayLe(17876) }

    private val CURVE: MultiSpline

    init {
        val s = S15
        val p = ParabolaOfStephensonMorrisonHohenkerk2016
        val pd = p.derivative
        val sd = s.derivative

        val longTermParabolaWidth = p.width

        // How many years wide we make the splines that connect the tables to the long-term parabola.
        val patchWidth = 800.0

        // To the left of the Table-S15 splines, design a spline connecting
        // them to the long-term parabola.

        var x1 = s.lower[0]  // For the current table, this = -720.0.
        var x0 = x1 - patchWidth
        val left = SingleSpline.makeSplineGivenEnds(x0, p.compute(x0), pd.compute(x0), x1, s.compute(x1), sd.compute(x1))

        // And to the left of that, put the pure long-term parabola.

        x1 = x0
        x0 = x1 - longTermParabolaWidth
        val farLeft = SingleSpline.makeSplineGivenEnds(x0, p.compute(x0), pd.compute(x0), x1, p.compute(x1), pd.compute(x1))

        // Truncate the splines table where the daily table starts, and
        // adjust the final spline to remove any discontinuity.

        val x = (DAILY_TT[0] - 1721045.0) / 365.25 // TT to J centuries

        val i = S15[0].binarySearch(x).let { if (it < 0) -it - 1 else it }
        val k = i - 1

        val desiredY = DAILY_DELTA_T[0]
        val currentY = s.compute(x)

        x0 = S15[0][k]
        x1 = S15[1][k]
        val t = (x - x0) / (x1 - x0)
        val a1 = S15[4][k] + (desiredY - currentY) / t // adjust linear term

        // To the right of the recent ∆T table, design a spline connecting
        // smoothly to the long-term parabola.

        x0 = (DAILY_TT.last() - 1721045.0) / 365.25  // TT to J centuries
        x1 = ((x0 + patchWidth) / 100.0).toInt() * 100.0  // Choose multiple of 100 years
        val y0 = DAILY_DELTA_T.last()

        val lookback = min(366, DAILY_DELTA_T.size)

        // Slope of last year of ∆T.
        val slope = (DAILY_DELTA_T.last() - DAILY_DELTA_T[DAILY_DELTA_T.size - lookback]) * lookback / 365.0

        val right = SingleSpline.makeSplineGivenEnds(x0, y0, slope, x1, p.compute(x1), pd.compute(x1))

        // At the far right, finish with the pure long-term parabola.

        x0 = x1
        x1 = x0 + longTermParabolaWidth
        val farRight = SingleSpline.makeSplineGivenEnds(x0, p.compute(x0), pd.compute(x0), x1, p.compute(x1), pd.compute(x1))

        val curve = (0..5).map { DoubleArray(i + 4) }

        for (m in 0..5) curve[m][0] = farLeft[m]
        for (m in 0..5) curve[m][1] = left[m]
        for (m in 0..5) S15[m].copyInto(curve[m], 2, 0, i)
        for (m in 0..5) curve[m][i + 2] = right[m]
        for (m in 0..5) curve[m][i + 3] = farRight[m]

        curve[4][i + 1] = a1

        CURVE = MultiSpline(curve)
    }

    override fun delta(time: InstantOfTime) = CURVE.compute(time.value)
}
