import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.QuadraticFitting

class QuadraticFittingTest : StringSpec() {

    init {
        "perfect V-curve" {
            // (x-5)² + 2
            val points = listOf(
                CurvePoint(1.0, 18.0),
                CurvePoint(2.0, 11.0),
                CurvePoint(3.0, 6.0),
                CurvePoint(4.0, 3.0),
                CurvePoint(5.0, 2.0),
                CurvePoint(6.0, 3.0),
                CurvePoint(7.0, 6.0),
                CurvePoint(8.0, 11.0),
                CurvePoint(9.0, 18.0),
            )

            val curve = QuadraticFitting.calculate(points)

            curve(5.0) shouldBeExactly 2.0
            curve.minimumX shouldBe (5.0 plusOrMinus 1e-12)
            curve.minimumY shouldBe (2.0 plusOrMinus 1e-12)
            curve.rSquared shouldBe (1.0 plusOrMinus 1e-12)
        }
    }
}
