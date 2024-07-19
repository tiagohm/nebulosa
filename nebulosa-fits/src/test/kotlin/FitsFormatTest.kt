import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.fits.isFits
import nebulosa.test.M82_COLOR_16_XISF
import nebulosa.test.NGC3344_COLOR_8_FITS
import org.junit.jupiter.api.Test

class FitsFormatTest {

    @Test
    fun shouldBeFitsFormat() {
        NGC3344_COLOR_8_FITS.isFits().shouldBeTrue()
        M82_COLOR_16_XISF.isFits().shouldBeFalse()
    }
}
