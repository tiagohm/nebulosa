import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.skycatalog.hyg.HygDatabase

@EnabledIf(NonGitHubOnlyCondition::class)
class HygDatabaseTest : StringSpec() {

    init {
        "load" {
            val database = HygDatabase()
            resource("hygdata_v3.csv")!!.use(database::load)
            database.size shouldBe 118005
            database.searchBy("Alp Psc") shouldHaveSize 1
        }
    }
}
