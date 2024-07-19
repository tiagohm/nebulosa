import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurveFitting
import nebulosa.curve.fitting.QuadraticFitting
import org.junit.jupiter.api.Test

class QuadraticFittingTest : CurveFitting<QuadraticFitting.Curve> by QuadraticFitting {

    @Test
    fun perfectVCurve() {
        // (x-5)² + 2
        val curve = calculate(
            1.0, 18.0, 2.0, 11.0, 3.0, 6.0,
            4.0, 3.0, 5.0, 2.0, 6.0, 3.0,
            7.0, 6.0, 8.0, 11.0, 9.0, 18.0,
        )

        curve(5.0) shouldBeExactly 2.0
        curve.minimum.x shouldBe (5.0 plusOrMinus 1e-12)
        curve.minimum.y shouldBe (2.0 plusOrMinus 1e-12)
        curve.rSquared shouldBe (1.0 plusOrMinus 1e-12)
    }
}
