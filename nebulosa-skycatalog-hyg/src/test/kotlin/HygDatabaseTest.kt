import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.test.download
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.io.path.inputStream

@Disabled
class HygDatabaseTest {

    @Test
    fun load() {
        val database = HygDatabase()
        // Typo at constellation name 119628 "Eco"
        val hygCsv = download("https://github.com/astronexus/HYG-Database/raw/main/hyg/CURRENT/hygdata_v41.csv")
        hygCsv.inputStream().use(database::load)
        database.size shouldBe 118002
        database.withText("Alp Psc") shouldHaveSize 1
    }
}
