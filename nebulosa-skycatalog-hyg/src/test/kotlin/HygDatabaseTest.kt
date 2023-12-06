import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.test.NonGitHubOnlyCondition

@EnabledIf(NonGitHubOnlyCondition::class)
class HygDatabaseTest : StringSpec() {

    init {
        "load" {
            val database = HygDatabase()
            resource("hyg_v35.csv")!!.use(database::load)
            database.size shouldBe 118002
            database.withText("Alp Psc") shouldHaveSize 1
        }
    }
}
