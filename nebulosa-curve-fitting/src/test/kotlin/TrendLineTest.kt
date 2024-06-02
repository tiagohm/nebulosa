import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
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

            line.slope shouldBeExactly 1.0
            line.intercept shouldBeExactly 0.0
        }
        "multiple points" {
            val line = TrendLine(1.0, 10.0, 2.0, 8.0, 3.0, 6.0, 4.0, 4.0)

            line.slope shouldBeExactly -2.0
            line.intercept shouldBeExactly 12.0
        }
    }
}
