import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.TrendLineRegression
import org.junit.jupiter.api.Test

class TrendLineRegressionTest {

    val x = doubleArrayOf(
        1.0, 1.47368421, 1.94736842, 2.42105263, 2.89473684, 3.36842105,
        3.84210526, 4.31578947, 4.78947368, 5.26315789, 5.73684211, 6.21052632,
        6.68421053, 7.15789474, 7.63157895, 8.10526316, 8.57894737, 9.05263158,
        9.52631579, 10.0
    )

    val y = doubleArrayOf(
        4.76405235, 4.53707378, 6.80070028, 11.26742564, 12.1955626, 4.44495872,
        12.33455024, 8.9783531, 10.08458339, 13.68736054, 13.30003943, 22.45285652,
        19.45535743, 16.18672643, 19.6505352, 19.91504455, 30.97552047, 17.24804098,
        23.03501337, 12.45904261
    )

    val weights = doubleArrayOf(
        1.0, 0.67857143, 0.51351351, 0.41304348, 0.34545455, 0.296875,
        0.26027397, 0.23170732, 0.20879121, 0.19, 0.17431193, 0.16101695,
        0.1496063, 0.13970588, 0.13103448, 0.12337662, 0.11656442, 0.11046512,
        0.10497238, 0.1
    )

    @Test
    fun olsMultipleLinear() {
        val points = x.indices.map { CurvePoint(x[it], y[it], weights[it]) }
        val parameters = TrendLineRegression.OLSMultipleLinear.compute(points)
        parameters.slope shouldBe (2.0541 plusOrMinus 1e-4)
        parameters.intercept shouldBe (2.8909 plusOrMinus 1e-4)
    }

    @Test
    fun simple() {
        val points = x.indices.map { CurvePoint(x[it], y[it], weights[it]) }
        val parameters = TrendLineRegression.Simple.compute(points)
        parameters.slope shouldBe (1.215 plusOrMinus 1e-4) // ???
        parameters.intercept shouldBe (3.3235 plusOrMinus 1e-4) // ???
    }

    @Test
    fun simpleWithoutWeight() {
        val points = x.indices.map { CurvePoint(x[it], y[it], 1.0) }
        val parameters = TrendLineRegression.Simple.compute(points)
        parameters.slope shouldBe (1.9228 plusOrMinus 1e-4)
        parameters.intercept shouldBe (3.6128 plusOrMinus 1e-4)
        parameters.rSquared shouldBe (0.59 plusOrMinus 1e-2)
    }
}
