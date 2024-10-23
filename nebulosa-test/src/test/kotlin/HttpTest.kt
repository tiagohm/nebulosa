import io.kotest.matchers.shouldBe
import nebulosa.test.extensionFromUrl
import org.junit.jupiter.api.Test

class HttpTest {

    @Test
    fun extensionFromUrl() {
        "https://hpiers.obspm.fr/iers/eop/eopc04/eopc04.1962-now".extensionFromUrl shouldBe "1962-now"
        "https://maia.usno.navy.mil/ser7/finals2000A.all".extensionFromUrl shouldBe "all"
        "https://github.com/dstndstn/astrometry.net/blob/main/demo/apod1.jpg?raw=true".extensionFromUrl shouldBe "jpg"
    }
}
