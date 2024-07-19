import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.skycatalog.sao.SaoCatalog
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import okio.source
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class SaoCatalogTest {

    @Test
    fun load() {
        val catalog = SaoCatalog()
        dataDirectory.concat("SAO.pc").source().use(catalog::load)
        catalog shouldHaveSize 258997

        catalog.first().id shouldBe 1
        catalog.first().rightAscensionJ2000 shouldBe ("0  00  05.097".hours plusOrMinus 1e-14)
        catalog.first().declinationJ2000 shouldBe ("82  41  41.82".deg plusOrMinus 1e-14)
        catalog.first().magnitude shouldBe 7.2
        catalog.first().spType shouldBe "A0"

        catalog.last().id shouldBe 258997
        catalog.last().rightAscensionJ2000 shouldBe ("23 58 52.487".hours plusOrMinus 1e-14)
        catalog.last().declinationJ2000 shouldBe ("-83 48 05.02".deg plusOrMinus 1e-14)
        catalog.last().magnitude shouldBe 8.9
        catalog.last().spType shouldBe "K0"
    }
}
