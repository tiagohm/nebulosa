import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.skycatalog.stellarium.Nebula
import nebulosa.test.AbstractTest
import nebulosa.test.download
import okio.gzip
import okio.source
import org.junit.jupiter.api.Test

class NebulaTest : AbstractTest() {

    @Test
    fun load() {
        val catalogPath = download("https://github.com/Stellarium/stellarium/raw/master/nebulae/default/catalog.dat")
        val catalog = catalogPath.source().gzip().autoClose()

        val namesPath = download("https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat")
        val names = namesPath.source().autoClose()

        val nebula = Nebula()
        nebula.load(catalog, names)

        nebula.size shouldBeExactly 94660

        nebula
            .searchAround("05 35 16.8".hours, "-05 23 24".deg, 1.0.deg)
            .onEach { println(it) }
            .size shouldBeExactly 11

        nebula
            .searchAround("18 02 42.0".hours, "-22 58 18".deg, 1.0.deg)
            .onEach { println(it) }
            .size shouldBeExactly 19
    }

    @Test
    fun names() {
        val namesPath = download("https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat")
        val names = namesPath.source().use(Nebula::namesFor)
        val thorHelmet = names.filter { it.id == "NGC 2359" } shouldHaveSize 5
        thorHelmet.map { it.name }.shouldContainAll("Thor's Helmet", "Duck Head Nebula", "Flying Eye Nebula", "Duck Nebula", "Whistle Nebula")
    }
}
