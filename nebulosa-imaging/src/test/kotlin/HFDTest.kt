import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindResult
import nebulosa.imaging.algorithms.star.hfd.HalfFluxDiameter
import nom.tam.fits.Fits

class HFDTest : StringSpec() {

    init {
        val image1 = Image.open(Fits("src/test/resources/HFD.1.fits"))
        val image2 = Image.open(Fits("src/test/resources/HFD.2.fits"))

        "ok" {
            val hfd = HalfFluxDiameter(542f, 974f)
            val star = hfd.compute(image1)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (13840.811f plusOrMinus 1f)
            star.snr shouldBe (82.5f plusOrMinus 1f)
            star.hfd shouldBe (2.9f plusOrMinus 1f)
            star.peak shouldBe (1240f plusOrMinus 1f)
            star.x.toInt() shouldBeExactly 544
            star.y.toInt() shouldBeExactly 974
        }
        "faint star" {
            val hfd = HalfFluxDiameter(981f, 327f)
            val star = hfd.compute(image1)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (113f plusOrMinus 1f)
            star.snr shouldBe (6.5f plusOrMinus 1f)
            star.hfd shouldBe (2f plusOrMinus 1f)
            star.peak shouldBe (22f plusOrMinus 1f)
            star.x.toInt() shouldBeExactly 980
            star.y.toInt() shouldBeExactly 327
        }
        "unfocused star" {
            val hfd = HalfFluxDiameter(303f, 177f)
            val star = hfd.compute(image2)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (1260.0212f plusOrMinus 1f)
            star.snr shouldBe (17.8989f plusOrMinus 1f)
            star.hfd shouldBe (6.52f plusOrMinus 1f)
            star.peak shouldBe (71f plusOrMinus 1f)
            star.x.toInt() shouldBeExactly 305
            star.y.toInt() shouldBeExactly 178
        }
        "low mass" {
            val hfd = HalfFluxDiameter(827f, 699f)
            val star = hfd.compute(image2)

            star.result shouldBe FindResult.LOWMASS
            star.mass shouldBeExactly 0f
            star.snr shouldBeExactly 0f
            star.hfd shouldBeExactly 0f
            star.peak shouldBeExactly 10f
            star.x.toInt() shouldBeExactly 827
            star.y.toInt() shouldBeExactly 699
        }
    }
}
