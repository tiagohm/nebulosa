import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Histogram
import nom.tam.fits.Fits

class HistogramTest : StringSpec() {

    init {
        "compute" {
            val image = Image.open(Fits("src/test/resources/M51.8.Mono.fits"))
            val histogram = Histogram()
            histogram.compute(image)
            histogram.median shouldBe (0.059f plusOrMinus 1e-3f)
            histogram.maxCount shouldBeExactly 86292
            histogram.maxValue shouldBe (1.0f plusOrMinus 1e-3f)
            histogram.pixelSum shouldBe (62033.0f plusOrMinus 1e-0f)
            histogram.pixelAvg shouldBe (0.073f plusOrMinus 1e-3f)
            histogram.stdDev shouldBe (0.072f plusOrMinus 1e-3f)
        }
    }
}
