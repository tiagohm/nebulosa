import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.fits.FitsHeaderCard
import nebulosa.fits.FitsHeaderCardFormatter

class FitsHeaderCardFormatterTest : StringSpec() {

    init {
        "should format boolean value" {
            val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("SIMPLE", true, "Boolean Type"))
            text shouldBe "SIMPLE  =                    T / Boolean Type                                   "
        }
        "should format integer value" {
            val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("NAXIS", 3, "Integer Type"))
            text shouldBe "NAXIS   =                    3 / Integer Type                                   "
        }
        "should format decimal value" {
            val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("EXPOSURE", 150.0, "Decimal Type"))
            text shouldBe "EXPOSURE=                150.0 / Decimal Type                                   "
        }
        "should format text value" {
            val text = FitsHeaderCardFormatter.format(FitsHeaderCard.create("INSTRUME", "Camera", "Text Type"))
            text shouldBe "INSTRUME= 'Camera  '           / Text Type                                      "
        }
        "should format end key" {
            val text = FitsHeaderCardFormatter.format(FitsHeaderCard.END)
            text shouldBe "END                                                                             "
        }
    }
}
