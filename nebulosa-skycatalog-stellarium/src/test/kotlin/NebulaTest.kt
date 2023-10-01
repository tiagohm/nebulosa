import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.io.resource
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.skycatalog.stellarium.Nebula
import okio.gzip
import okio.source

@EnabledIf(NonGitHubOnlyCondition::class)
class NebulaTest : StringSpec() {

    init {
        val nebula = Nebula()
        val catalog = resource("catalog.dat")!!.source().gzip()
        val names = resource("names.dat")!!.source()
        nebula.load(catalog, names)

        "load" {
            nebula.size shouldBeExactly 94661
        }
        "search around" {
            nebula
                .searchAround("05 35 16.8".hours, "-05 23 24".deg, 1.0.deg)
                .onEach { println(it) }
                .size shouldBeExactly 11

            nebula
                .searchAround("18 02 42.0".hours, "-22 58 18".deg, 1.0.deg)
                .onEach { println(it) }
                .size shouldBeExactly 19
        }
    }
}
