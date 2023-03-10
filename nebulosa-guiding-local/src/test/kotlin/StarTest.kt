import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.guiding.local.Star
import nebulosa.imaging.Image
import nom.tam.fits.Fits

class StarTest : StringSpec() {

    init {
        val fits1 = Image.open(Fits("src/test/resources/1.fits"))
        val fits2 = Image.open(Fits("src/test/resources/2.fits"))

        "star found" {
            with(Star(542f, 974f)) {
                find(fits1, 15f).shouldBeTrue()

                mass shouldBe (13840.811f plusOrMinus 0.1f)
                snr shouldBe (82.5f plusOrMinus 0.1f)
                hfd shouldBe (2.9f plusOrMinus 0.1f)
                peak shouldBe (1240f plusOrMinus 1f)
                x.toInt() shouldBeExactly 544
                y.toInt() shouldBeExactly 974
            }
        }
        "no star" {
            with(Star(68f, 35f)) {
                find(fits1, 15f).shouldBeFalse()

                mass shouldBe (0f plusOrMinus 0.1f)
                snr shouldBe (0f plusOrMinus 0.1f)
                hfd shouldBe (0f plusOrMinus 0.1f)
                peak shouldBe (9f plusOrMinus 1f)
                x.toInt() shouldBeExactly 68
                y.toInt() shouldBeExactly 35
            }
        }
        "faint star" {
            with(Star(996f, 250f)) {
                find(fits1, 15f).shouldBeTrue()

                // mass shouldBe (740f plusOrMinus 0.1f)
                // snr shouldBe (18.1f plusOrMinus 0.1f)
                // hfd shouldBe (2.5f plusOrMinus 0.1f)
                // peak shouldBe (80f plusOrMinus 1f)
                // x.toInt() shouldBeExactly 996
                // y.toInt() shouldBeExactly 249
            }
        }
        "unfocused star" {
            with(Star(303f, 177f)) {
                find(fits2, 15f).shouldBeTrue()

                mass shouldBe (1260.0212f plusOrMinus 2e-4f)
                snr shouldBe (17.8989f plusOrMinus 1e-4f)
                hfd shouldBe (6.52f plusOrMinus 1e-2f)
                peak shouldBe (71f plusOrMinus 1f)
                x.toInt() shouldBeExactly 305
                y.toInt() shouldBeExactly 178
            }

            with(Star(827f, 699f)) {
                find(fits2, 15f).shouldBeFalse()

                mass shouldBeExactly 0f
                snr shouldBeExactly 0f
                hfd shouldBeExactly 0f
                peak shouldBeExactly 10f
                x.toInt() shouldBeExactly 827
                y.toInt() shouldBeExactly 699
            }
        }
    }
}
