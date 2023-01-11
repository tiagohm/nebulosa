import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.guiding.FindMode
import nebulosa.guiding.Star
import nebulosa.imaging.FitsImage
import nom.tam.fits.Fits

class StarTest : StringSpec() {

    init {
        "centroid" {
            // X=510.00, Y=956.00, Mass=13854, SNR=82.6, Peak=1440 HFD=2.8
            val fits = FitsImage(Fits("src/test/resources/1.fits"))
            val star = Star(510, 956)
            star.find(fits, 15, FindMode.CENTROID, 1.5f).shouldBeTrue()

            star.mass shouldBe (13854f plusOrMinus 100f)
            star.snr shouldBe (82f plusOrMinus 0.5f)
            star.hfd shouldBe (2.8f plusOrMinus 0.1f)
            star.peak shouldBe (1440f plusOrMinus 10f)
            star.x shouldBe (510f plusOrMinus 12f)
            star.y shouldBe (964f plusOrMinus 12f)
        }
    }
}
