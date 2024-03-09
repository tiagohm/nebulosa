import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurveFitting
import nebulosa.curve.fitting.HyperbolicFitting

class HyperbolicFittingTest : StringSpec(), CurveFitting<HyperbolicFitting.Curve> by HyperbolicFitting {

    init {
        "perfect V-curve with only one minimum point" {
            val curve = calculate(
                1.0, 18.0, 2.0, 11.0, 3.0, 6.0, 4.0, 3.0, 5.0, 2.0,
                6.0, 3.0, 7.0, 6.0, 8.0, 11.0, 9.0, 18.0,
            )

            curve.minimumX shouldBe (5.0 plusOrMinus 1e-12)
            curve.minimumY shouldBe (1.2 plusOrMinus 1e-12)
        }
        "bad data:prevent infinit loop" {
            shouldThrow<IllegalArgumentException> { calculate(1000.0, 18.0, 1100.0, 0.0, 1200.0, 0.0) }
                .message shouldBe "Not enough valid data points to fit a curve."
            shouldThrow<IllegalArgumentException> { calculate(1000.0, 18.0, 1000.0, 18.0, 1000.0, 18.0, 1100.0, 0.0, 1200.0, 0.0) }
                .message shouldBe "Not enough valid data points to fit a curve."
            shouldThrow<IllegalArgumentException> { calculate(900.0, 18.0, 1000.0, 18.0, 1000.0, 18.0, 1100.0, 0.0, 1200.0, 0.0) }
                .message shouldBe "Not enough valid data points to fit a curve."
            shouldThrow<IllegalArgumentException> { calculate(800.0, 18.0, 900.0, 0.0, 1000.0, 0.0, 1000.0, 18.0, 1100.0, 0.0, 1200.0, 0.0) }
                .message shouldBe "Not enough valid data points to fit a curve."
        }
    }
}
