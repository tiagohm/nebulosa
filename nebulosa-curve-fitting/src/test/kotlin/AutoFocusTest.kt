import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.CurvePoint.Companion.midPoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import kotlin.math.roundToInt

class AutoFocusTest : StringSpec() {

    init {
        // The best focus is 8000.

        "near:hyperbolic" {
            val points = focusPointsNearBestFocus()
            val curve = HyperbolicFitting.calculate(points)
            curve.minimum.x.roundToInt() shouldBeExactly 8031
            curve.rSquared shouldBe (0.89 plusOrMinus 1e-2)
        }
        "near:parabolic" {
            val points = focusPointsNearBestFocus()
            val curve = QuadraticFitting.calculate(points)
            curve.minimum.x.roundToInt() shouldBeExactly 8051
            curve.rSquared shouldBe (0.74 plusOrMinus 1e-2)
        }
        "near:trendline" {
            val points = focusPointsNearBestFocus()
            val line = TrendLineFitting.calculate(points)
            line.minimum.x.roundToInt() shouldBeExactly 8100
            line.rSquared shouldBe (0.94 plusOrMinus 1e-2)
        }
        "near:hyperbolic + trendline" {
            val points = focusPointsNearBestFocus()
            val curve = HyperbolicFitting.calculate(points)
            val line = TrendLineFitting.calculate(points)
            (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 7952
        }
        "near:parabolic + trendline" {
            val points = focusPointsNearBestFocus()
            val curve = QuadraticFitting.calculate(points)
            val line = TrendLineFitting.calculate(points)
            (curve.minimum midPoint line.intersection).x.roundToInt() shouldBeExactly 7962
        }
    }

    companion object {

        @JvmStatic
        private fun focusPointsNearBestFocus() = listOf(
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
    }
}
