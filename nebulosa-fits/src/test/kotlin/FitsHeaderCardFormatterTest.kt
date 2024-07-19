import io.kotest.matchers.shouldBe
import nebulosa.fits.FitsHeaderCard
import nebulosa.fits.FitsHeaderCardFormatter
import org.junit.jupiter.api.Test

class FitsHeaderCardFormatterTest {

    @Test
    fun shouldFormatBooleanValue() {
        val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("SIMPLE", true, "Boolean Type"))
        text shouldBe "SIMPLE  =                    T / Boolean Type                                   "
    }

    @Test
    fun shouldFormatIntegerValue() {
        val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("NAXIS", 3, "Integer Type"))
        text shouldBe "NAXIS   =                    3 / Integer Type                                   "
    }

    @Test
    fun shouldFormatDecimalValue() {
        val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("EXPOSURE", 150.0, "Decimal Type"))
        text shouldBe "EXPOSURE=                150.0 / Decimal Type                                   "
    }

    @Test
    fun shouldFormatTextValue() {
        val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("INSTRUME", "Camera", "Text Type"))
        text shouldBe "INSTRUME= 'Camera  '           / Text Type                                      "
    }

    @Test
    fun shouldFormatEndKey() {
        val text = FitsHeaderCardFormatter.format(FitsHeaderCard.END)
        text shouldBe "END                                                                             "
    }
}
