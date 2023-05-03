import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
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
            histogram.median shouldBe (3856f plusOrMinus 1e-3f)
            histogram.maxCount shouldBeExactly 86292
            histogram.maxValue shouldBe (65535.0f plusOrMinus 1e-3f)
            histogram.pixelSum shouldBeExactly 4076999427L
            histogram.pixelAvg shouldBe (4794.2026f plusOrMinus 1e-3f)
            histogram.stdDev shouldBe (4742.203f plusOrMinus 1e-3f)
        }
    }
}
