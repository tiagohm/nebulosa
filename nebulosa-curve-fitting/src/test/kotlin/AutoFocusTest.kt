import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.CurvePoint.Companion.midPoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

class AutoFocusTest {

    // ASCOM Sky Simulator: The best focus is 8000.

    @Test
    fun ascomHyperbolic() {
        val curve = HyperbolicFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        curve.minimum.x.roundToInt() shouldBeExactly 8031
        curve.rSquared shouldBe (0.89 plusOrMinus 1e-2)
    }

    @Test
    fun ascomParabolic() {
        val curve = QuadraticFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        curve.minimum.x.roundToInt() shouldBeExactly 8051
        curve.rSquared shouldBe (0.74 plusOrMinus 1e-2)
    }

    @Test
    fun ascomTrendline() {
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        line.intersection.x.roundToInt() shouldBeExactly 7873
        line.rSquared shouldBe (0.99 plusOrMinus 1e-2)
    }

    @Test
    fun ascomHyperbolicAndTrendline() {
        val curve = HyperbolicFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 7952
    }

    @Test
    fun ascomParabolicAndTrendline() {
        val curve = QuadraticFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_ASCOM)
        (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 7962
    }

    // INDI CCD Simulator: The best focus is 36700.

    @Test
    fun indiHyperbolic() {
        val curve = HyperbolicFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        curve.minimum.x.roundToInt() shouldBeExactly 36500
        curve.rSquared shouldBe (0.84 plusOrMinus 1e-2)
    }

    @Test
    fun indiParabolic() {
        val curve = QuadraticFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        curve.minimum.x.roundToInt() shouldBeExactly 36829
        curve.rSquared shouldBe (0.99 plusOrMinus 1e-2)
    }

    @Test
    fun indiTrendline() {
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        line.intersection.x.roundToInt() shouldBeExactly 39445
        line.rSquared shouldBe (0.99 plusOrMinus 1e-2)
    }

    @Test
    fun indiHyperbolicAndTrendline() {
        val curve = HyperbolicFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 37973
    }

    @Test
    fun indiParabolicAndTrendline() {
        val curve = QuadraticFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_INDI_WITH_WEIGHTS)
        (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 38137
    }

    @Test
    fun indiHyperbolicWithoutWeights() {
        val curve = HyperbolicFitting.calculate(FOCUS_POINTS_BY_INDI)
        curve.minimum.x.roundToInt() shouldBeExactly 36000
        curve.rSquared shouldBe (0.95 plusOrMinus 1e-2)
    }

    @Test
    fun indiParabolicWithoutWeights() {
        val curve = QuadraticFitting.calculate(FOCUS_POINTS_BY_INDI)
        curve.minimum.x.roundToInt() shouldBeExactly 36815
        curve.rSquared shouldBe (0.99 plusOrMinus 1e-2)
    }

    @Test
    fun indiTrendlineWithoutWeights() {
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_INDI)
        line.intersection.x.roundToInt() shouldBeExactly 39653
        line.rSquared shouldBe (0.99 plusOrMinus 1e-2)
    }

    @Test
    fun indiHyperbolicAndTrendlineWithoutWeights() {
        val curve = HyperbolicFitting.calculate(FOCUS_POINTS_BY_INDI)
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_INDI)
        (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 37826
    }

    @Test
    fun indiParabolicAndTrendlineWithoutWeights() {
        val curve = QuadraticFitting.calculate(FOCUS_POINTS_BY_INDI)
        val line = TrendLineFitting.calculate(FOCUS_POINTS_BY_INDI)
        (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 38234
    }

    companion object {

        @JvmStatic val FOCUS_POINTS_BY_ASCOM = listOf(
            CurvePoint(10100.0, 13.892408928571431),
            CurvePoint(9600.0, 12.879208888888888),
            CurvePoint(9100.0, 10.640856213017754),
            CurvePoint(8600.0, 6.891483673469387),
            CurvePoint(8100.0, 2.9738176470588247),
            CurvePoint(7600.0, 5.063299489795917),
            CurvePoint(7100.0, 9.326303846153845),
            CurvePoint(6600.0, 12.428210576923071),
            CurvePoint(6100.0, 13.662644615384618),
        )

        @JvmStatic val FOCUS_POINTS_BY_INDI_WITH_WEIGHTS = listOf(
            CurvePoint(0.0, 8.31, 0.4455),
            CurvePoint(10000.0, 5.32, 0.2508),
            CurvePoint(20000.0, 3.03, 0.1583),
            CurvePoint(30000.0, 1.86, 0.1089),
            CurvePoint(40000.0, 1.67, 0.0879),
            CurvePoint(50000.0, 2.47, 0.1584),
            CurvePoint(60000.0, 4.47, 0.1991),
            CurvePoint(70000.0, 7.29, 0.4272),
            CurvePoint(80000.0, 10.63, 0.6760),
            // CurvePoint(90000.0, 15.17, 0.4987),
            // CurvePoint(100000.0, 19.74, 1.2278),
        )

        @JvmStatic val FOCUS_POINTS_BY_INDI = FOCUS_POINTS_BY_INDI_WITH_WEIGHTS
            .map { CurvePoint(it.x, it.y) }
    }
}
