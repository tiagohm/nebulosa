import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.SubFrame
import nebulosa.imaging.hfd.FindResult
import nebulosa.imaging.hfd.HalfFluxDiameter
import nebulosa.test.FitsStringSpec
import kotlin.math.roundToInt

class HFDTest : FitsStringSpec() {

    init {
        val image = Image.open(NGC3344_MONO_16)

        "ok" {
            val hfd = HalfFluxDiameter(89.0, 138.0)
            val star = hfd.compute(image)

            star.result shouldBe FindResult.OK
            star.mass shouldBe (990716.0 plusOrMinus 1.0)
            star.snr shouldBe (39.9 plusOrMinus 0.1)
            star.hfd shouldBe (4.85 plusOrMinus 0.1)
            star.peak shouldBe (64250.0 plusOrMinus 1.0)
            star.x.roundToInt() shouldBeExactly 89
            star.y.roundToInt() shouldBeExactly 138

            image.transform(SubFrame.centered(89, 138, 15)).save("hfd-ok")
        }
        "low hfd" {
            val hfd = HalfFluxDiameter(234.0, 143.0, 7.0)
            val star = hfd.compute(image)

            star.result shouldBe FindResult.LOWHFD
            star.mass shouldBe (76195.4 plusOrMinus 1.0)
            star.snr shouldBe (26.5 plusOrMinus 0.1)
            star.hfd shouldBe (1.41 plusOrMinus 0.1)
            star.peak shouldBe (37265.0 plusOrMinus 1.0)
            star.x.roundToInt() shouldBeExactly 234
            star.y.roundToInt() shouldBeExactly 143

            image.transform(SubFrame.centered(234, 143, 7)).save("hfd-low-hfd")
        }
        "low snr" {
            val hfd = HalfFluxDiameter(233.0, 14.0, 7.0)
            val star = hfd.compute(image)

            star.result shouldBe FindResult.LOWSNR
            star.snr shouldBe (2.9 plusOrMinus 0.1)
            star.hfd shouldBe (0.0 plusOrMinus 0.1)
            star.peak shouldBe (20303.0 plusOrMinus 1.0)

            image.transform(SubFrame.centered(233, 14, 7)).save("hfd-low-snr")
        }
        "low mass" {
            val hfd = HalfFluxDiameter(97.0, 64.0, 3.0)
            val star = hfd.compute(image)

            star.result shouldBe FindResult.LOWMASS
            star.mass shouldBe (0.0 plusOrMinus 0.1)
            star.snr shouldBe (0.0 plusOrMinus 0.1)
            star.hfd shouldBe (0.0 plusOrMinus 0.1)
            star.peak shouldBe (27242.0 plusOrMinus 1.0)

            image.transform(SubFrame.centered(97, 64, 3)).save("hfd-low-mass")
        }
    }
}
