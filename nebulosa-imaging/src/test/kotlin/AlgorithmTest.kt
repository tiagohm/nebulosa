import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Median
import nebulosa.imaging.algorithms.Statistics
import nom.tam.fits.Fits

class AlgorithmTest : StringSpec() {

    init {
        "Median" {
            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
            Image.openFITS(fits).compute(Median()) shouldBe (0.0000763f plusOrMinus 1e-8f)
        }
        "Statistics" {
            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
            val statistics = Image.openFITS(fits).compute(Statistics())

            statistics.count shouldBeExactly 1310720
            statistics.maxCount shouldBeExactly 131696
            statistics.mean shouldBe (0.000072f plusOrMinus 1e-6f)
            statistics.sumOfSquares shouldBe (0.009701115f plusOrMinus 1e-4f)
            statistics.median shouldBe (0.0000763f plusOrMinus 1e-8f)
            statistics.variance shouldBe (2.2e-9f plusOrMinus 1e-10f)
            statistics.stdDev shouldBe (0.0000469f plusOrMinus 1e-7f)
            statistics.avgDev shouldBe (0.0000382f plusOrMinus 1e-7f)
            statistics.minimum shouldBeExactly 0f
            statistics.maximum shouldBeExactly 0.0066071567f
        }
    }
}
