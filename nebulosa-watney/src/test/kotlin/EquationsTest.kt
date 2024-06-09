import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.watney.platesolver.math.solveLeastSquares

class EquationsTest : StringSpec() {

    init {
        "solve least squares" {
            val equations = listOf(
                doubleArrayOf(7.0, -6.0, 8.0, -15.0),
                doubleArrayOf(3.0, 5.0, -2.0, -27.0),
                doubleArrayOf(2.0, -2.0, 7.0, -20.0),
                doubleArrayOf(4.0, 2.0, -5.0, -2.0),
                doubleArrayOf(9.0, -8.0, 7.0, -5.0),
            )

            val (a, b, c) = solveLeastSquares(equations)
            a shouldBe (2.474 plusOrMinus 1e-3)
            b shouldBe (5.397 plusOrMinus 1e-3)
            c shouldBe (3.723 plusOrMinus 1e-3)
        }
    }
}
