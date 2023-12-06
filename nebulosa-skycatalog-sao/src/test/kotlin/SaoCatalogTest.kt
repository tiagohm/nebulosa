import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.bufferedResource
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.skycatalog.sao.SaoCatalog
import nebulosa.test.NonGitHubOnlyCondition

@EnabledIf(NonGitHubOnlyCondition::class)
class SaoCatalogTest : StringSpec() {

    init {
        "load" {
            val catalog = SaoCatalog()
            catalog.load(bufferedResource("SAO.pc")!!)
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
}
