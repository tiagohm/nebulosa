import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
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
                .searchAround(Angle.from("05 35 16.8", true), Angle.from("-05 23 24"), 1.0.deg)
                .onEach { println(it) }
                .size shouldBeExactly 11

            nebula
                .searchAround(Angle.from("18 02 42.0", true), Angle.from("-22 58 18"), 1.0.deg)
                .onEach { println(it) }
                .size shouldBeExactly 19
        }
    }
}
