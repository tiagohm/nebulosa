import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.skycatalog.stellarium.Nebula
import nebulosa.test.NonGitHubOnlyCondition
import okio.gzip
import okio.source
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class NebulaTest : StringSpec() {

    init {
        "load" {
            val catalog = Path.of("../data/catalog.dat").source().gzip()
            val names = Path.of("../data/names.dat").source()

            val nebula = Nebula()
            nebula.load(catalog, names)

            nebula.size shouldBeExactly 94661

            nebula
                .searchAround("05 35 16.8".hours, "-05 23 24".deg, 1.0.deg)
                .onEach { println(it) }
                .size shouldBeExactly 11

            nebula
                .searchAround("18 02 42.0".hours, "-22 58 18".deg, 1.0.deg)
                .onEach { println(it) }
                .size shouldBeExactly 19
        }
        "names" {
            val names = Path.of("../data/names.dat").source().use(Nebula::namesFor)
            val thorHelmet = names.filter { it.id == "NGC 2359" } shouldHaveSize 5
            thorHelmet.map { it.name }.shouldContainAll("Thor's Helmet", "Duck Head Nebula", "Flying Eye Nebula", "Duck Nebula", "Whistle Nebula")
        }
    }
}
