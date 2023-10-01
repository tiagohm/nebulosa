import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF

class ConstellationTest : StringSpec() {

    init {
        "find" {
            for (line in resource("CONSTELLATION_TEST.txt")!!.bufferedReader().lines()) {
                val parts = line.split(",")
                val ra = parts[0].toDouble()
                val dec = parts[1].toDouble()
                val name = parts[2]
                Constellation.find(ICRF.equatorial(ra.hours, dec.deg)).name shouldBe name
            }
        }
    }
}
