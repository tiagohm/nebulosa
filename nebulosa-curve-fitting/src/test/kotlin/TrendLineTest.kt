import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.TrendLine

class TrendLineTest : StringSpec() {

    init {
        "no points" {
            val line = TrendLine.ZERO

            line.slope shouldBeExactly 0.0
            line.intercept shouldBeExactly 0.0
        }
        "one point" {
            val line = TrendLine(5.0, 5.0)

            line.slope shouldBeExactly 0.0
            line.intercept shouldBeExactly 0.0
        }
        "two points" {
            val line = TrendLine(0.0, 0.0, 1.0, 1.0)

            line.slope shouldBe (1.0 plusOrMinus 1e-12)
            line.intercept shouldBe (0.0 plusOrMinus 1e-12)
        }
        "multiple points" {
            val line = TrendLine(1.0, 10.0, 2.0, 8.0, 3.0, 6.0, 4.0, 4.0)

            line.slope shouldBe (-2.0 plusOrMinus 1e-12)
            line.intercept shouldBe (12.0 plusOrMinus 1e-12)
        }
        "multiple points with weight" {
            val line = TrendLine(
                listOf(
                    CurvePoint(1.0, 10.0, 0.9),
                    CurvePoint(2.0, 8.0, 1.1),
                    CurvePoint(3.0, 6.0, 0.9),
                    CurvePoint(4.0, 4.0, 1.1),
                )
            )

            println(line.slope)

            line.slope shouldBe (-2.0 plusOrMinus 1e-2)
            line.intercept shouldBe (12.0 plusOrMinus 1e-2)
        }
    }
}
