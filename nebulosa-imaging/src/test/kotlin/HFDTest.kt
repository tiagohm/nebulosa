import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindResult
import nebulosa.imaging.algorithms.star.hfd.HalfFluxDiameter
import nom.tam.fits.Fits

class HFDTest : StringSpec() {

    init {
        val image1 = Image.openFITS(Fits("src/test/resources/HFD.1.fits"))
        val image2 = Image.openFITS(Fits("src/test/resources/HFD.2.fits"))

        "ok" {
            val hfd = HalfFluxDiameter(542.0, 974.0)
            val star = hfd.compute(image1)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (13840.811 plusOrMinus 1.0)
            star.snr shouldBe (82.5 plusOrMinus 1.0)
            star.hfd shouldBe (2.9 plusOrMinus 1.0)
            star.peak shouldBe (1240.0 plusOrMinus 1.0)
            star.x.toInt() shouldBeExactly 544
            star.y.toInt() shouldBeExactly 974
        }
        "faint star" {
            val hfd = HalfFluxDiameter(981.0, 327.0)
            val star = hfd.compute(image1)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (113.0 plusOrMinus 1.0)
            star.snr shouldBe (6.5 plusOrMinus 1.0)
            star.hfd shouldBe (2.0 plusOrMinus 1.0)
            star.peak shouldBe (22.0 plusOrMinus 1.0)
            star.x.toInt() shouldBeExactly 980
            star.y.toInt() shouldBeExactly 327
        }
        "unfocused star" {
            val hfd = HalfFluxDiameter(303.0, 177.0)
            val star = hfd.compute(image2)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (1260.0212 plusOrMinus 1.0)
            star.snr shouldBe (17.8989 plusOrMinus 1.0)
            star.hfd shouldBe (6.52 plusOrMinus 1.0)
            star.peak shouldBe (71.0 plusOrMinus 1.0)
            star.x.toInt() shouldBeExactly 305
            star.y.toInt() shouldBeExactly 178
        }
        "low mass" {
            val hfd = HalfFluxDiameter(827.0, 699.0)
            val star = hfd.compute(image2)

            star.result shouldBe FindResult.LOWMASS
            star.mass shouldBeExactly 0.0
            star.snr shouldBeExactly 0.0
            star.hfd shouldBeExactly 0.0
            star.peak shouldBe (10.0 plusOrMinus 1e-8)
            star.x.toInt() shouldBeExactly 827
            star.y.toInt() shouldBeExactly 699
        }
    }
}
