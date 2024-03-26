import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.fits.FitsHeaderCardParser
import nebulosa.fits.FitsHeaderCardType

class FitsHeaderCardParserTest : StringSpec() {

    init {
        "should parse boolean value" {
            val parsed = FitsHeaderCardParser("SIMPLE  =                    T / Boolean Type                                   ")

            parsed.key shouldBe "SIMPLE"
            parsed.value shouldBe "T"
            parsed.type shouldBe FitsHeaderCardType.BOOLEAN
            parsed.comment shouldBe "Boolean Type"
        }
        "should parse integer value" {
            val parsed = FitsHeaderCardParser("NAXIS   = 3 / Integer Type     ")

            parsed.key shouldBe "NAXIS"
            parsed.value shouldBe "3"
            parsed.type shouldBe FitsHeaderCardType.INTEGER
            parsed.comment shouldBe "Integer Type"
        }
        "should parse decimal value" {
            val parsed = FitsHeaderCardParser("EXPOSURE= 150.0 / Decimal Type ")

            parsed.key shouldBe "EXPOSURE"
            parsed.value shouldBe "150.0"
            parsed.type shouldBe FitsHeaderCardType.DECIMAL
            parsed.comment shouldBe "Decimal Type"
        }
        "should parse text value" {
            val parsed = FitsHeaderCardParser("INSTRUME= 'Camera  ' / Text Type ")

            parsed.key shouldBe "INSTRUME"
            parsed.value shouldBe "Camera"
            parsed.type shouldBe FitsHeaderCardType.TEXT
            parsed.comment shouldBe "Text Type"
        }
        "should parse end key" {
            val parsed = FitsHeaderCardParser("END")

            parsed.key shouldBe "END"
            parsed.value shouldBe ""
            parsed.type shouldBe FitsHeaderCardType.NONE
            parsed.comment shouldBe ""
        }
    }
}
