import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurveFitting
import nebulosa.curve.fitting.TrendLineFitting
import org.junit.jupiter.api.Test

class TrendLineFittingTest : CurveFitting<TrendLineFitting.Curve> by TrendLineFitting {

    @Test
    fun perfectVCurveWithOnlyOneMinimumPoint() {
        val curve = calculate(
            1.0, 10.0, 2.0, 8.0, 3.0, 6.0, 4.0, 4.0, // left
            9.0, 10.0, 8.0, 8.0, 7.0, 6.0, 6.0, 4.0, // right
            5.0, 2.0, // tip
        )

        curve.intersection.x shouldBe (5.0 plusOrMinus 1e-12)
        curve.intersection.y shouldBe (2.0 plusOrMinus 1e-12)
    }

    @Test
    fun perfectVCurveWithFlatTipWithMultiplePoints() {
        val curve = calculate(
            1.0, 10.0, 2.0, 8.0, 3.0, 6.0, 4.0, 4.0, // left
            11.0, 10.0, 10.0, 8.0, 9.0, 6.0, 8.0, 4.0, // right
            5.0, 2.1, 6.0, 2.0, 7.0, 2.1, // tip
        )

        curve.intersection.x shouldBe (6.0 plusOrMinus 1e-12)
        curve.intersection.y shouldBe (0.0 plusOrMinus 1e-12)
    }
}
