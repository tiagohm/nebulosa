import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.Image.Companion.asImage
import nebulosa.image.algorithms.computation.hfd.HFD
import nebulosa.test.fits.FOCUS_14_FITS
import org.junit.jupiter.api.Test

class HFDTest {

    @Test
    fun centered() {
        val image = FOCUS_14_FITS.fits().asImage()
        val star = image.compute(HFD(690, 276, 25))
        star.x shouldBeExactly 690
        star.y shouldBeExactly 276
        star.hfd shouldBe (2.88f plusOrMinus 0.1f)
        star.snr shouldBe (683.3849f plusOrMinus 0.1f)
        star.flux shouldBe (934029.75f plusOrMinus 0.1f)
    }

    @Test
    fun outOfCenter() {
        val image = FOCUS_14_FITS.fits().asImage()
        val star = image.compute(HFD(695, 279, 25))
        star.x shouldBeExactly 690
        star.y shouldBeExactly 276
        star.hfd shouldBe (2.88f plusOrMinus 0.1f)
        star.snr shouldBe (683.3849f plusOrMinus 0.1f)
        star.flux shouldBe (934029.75f plusOrMinus 0.1f)
    }
}
