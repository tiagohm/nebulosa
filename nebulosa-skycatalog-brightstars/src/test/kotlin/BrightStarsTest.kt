import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.skycatalog.brightstars.BrightStars

class BrightStarsTest : StringSpec() {

    init {
        "load" {
            BrightStars.size shouldBe 9225
        }
    }
}
