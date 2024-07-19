import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.TrendLine
import org.junit.jupiter.api.Test

class TrendLineTest {

    @Test
    fun noPoints() {
        val line = TrendLine.ZERO

        line.slope shouldBeExactly 0.0
        line.intercept shouldBeExactly 0.0
    }

    @Test
    fun onePoint() {
        val line = TrendLine(5.0, 5.0)

        line.slope shouldBeExactly 0.0
        line.intercept shouldBeExactly 0.0
    }

    @Test
    fun twoPoints() {
        val line = TrendLine(0.0, 0.0, 1.0, 1.0)

        line.slope shouldBe (1.0 plusOrMinus 1e-12)
        line.intercept shouldBe (0.0 plusOrMinus 1e-12)
    }

    @Test
    fun multiplePoints() {
        val line = TrendLine(1.0, 10.0, 2.0, 8.0, 3.0, 6.0, 4.0, 4.0)

        line.slope shouldBe (-2.0 plusOrMinus 1e-12)
        line.intercept shouldBe (12.0 plusOrMinus 1e-12)
    }

    @Test
    fun simpleWeightedRegression() {
        val line = TrendLine(
            CurvePoint(1.0, 1.0, 1.0),
            CurvePoint(2.0, 3.0, 1.0),
            CurvePoint(3.0, 2.0, 1.0),
            CurvePoint(4.0, 5.0, 2.0),
            CurvePoint(5.0, 4.0, 2.0),
        )

        line.slope shouldBe (0.7812 plusOrMinus 1e-4)
        line.intercept shouldBe (0.75 plusOrMinus 1e-2)
        // line.rSquared shouldBeExactly 0.61
    }

    @Test
    fun weightedRegressionWithHeteroscedasticData() {
        // np.random.seed(0)
        // X = np.linspace(1, 10, 100)
        // Y = 2 * X + 1 + np.random.normal(0, X)
        // W = 1 / X

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

        val points = x.indices.map { CurvePoint(x[it], y[it], weights[it]) }
        val line = TrendLine(points)

        line.slope shouldBe (2.0541 plusOrMinus 1e-4)
        line.intercept shouldBe (2.8909 plusOrMinus 1e-4)
        // line.rSquared shouldBeExactly 0.725
    }

    @Test
    fun standardOlsRegression() {
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

        val points = x.indices.map { CurvePoint(x[it], y[it]) }
        val line = TrendLine(points)

        line.slope shouldBe (1.9229 plusOrMinus 1e-4)
        line.intercept shouldBe (3.6128 plusOrMinus 1e-4)
        // line.rSquared shouldBeExactly 0.595
    }
}
