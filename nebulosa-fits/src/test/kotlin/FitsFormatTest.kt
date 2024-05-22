import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.fits.isFits
import nebulosa.test.AbstractFitsAndXisfTest

class FitsFormatTest : AbstractFitsAndXisfTest() {

    init {
        "should be fits format" {
            NGC3344_COLOR_8_FITS.isFits().shouldBeTrue()
            M82_COLOR_16_XISF.isFits().shouldBeFalse()
        }
    }
}
