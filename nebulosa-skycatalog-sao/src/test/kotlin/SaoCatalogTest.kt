import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.skycatalog.sao.SaoCatalog

@EnabledIf(NonGitHubOnlyCondition::class)
class SaoCatalogTest : StringSpec() {

    init {
        "load" {
            val catalog = SaoCatalog()
            val resource = resource("SAO.pc")!!
            catalog.load(resource)
            catalog shouldHaveSize 258997

            catalog.first().id shouldBe 1
            catalog.first().rightAscension.value shouldBe (Angle.from("0  00  05.097", true)!!.value plusOrMinus 1e-14)
            catalog.first().declination.value shouldBe (Angle.from("82  41  41.82")!!.value plusOrMinus 1e-14)
            catalog.first().magnitude shouldBe 7.2
            catalog.first().spType shouldBe "A0"

            catalog.last().id shouldBe 258997
            catalog.last().rightAscension.value shouldBe (Angle.from("23 58 52.487", true)!!.value plusOrMinus 1e-14)
            catalog.last().declination.value shouldBe (Angle.from("-83 48 05.02")!!.value plusOrMinus 1e-14)
            catalog.last().magnitude shouldBe 8.9
            catalog.last().spType shouldBe "K0"
        }
    }
}
