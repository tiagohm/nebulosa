import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nova.astrometry.Vsop87
import nebulosa.nova.position.Barycentric
import nebulosa.time.TDB
import nebulosa.time.TimeYMDHMS

@Suppress("FloatingPointLiteralPrecision")
class Vsop87Test : StringSpec() {

    init {

        timeout = 1000L

        "position of mars" {
            val mars = Vsop87.Body.MARS
            val time = TDB(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0))
            val barycentric = mars.at<Barycentric>(time)
            barycentric.position.a1 shouldBe (7.482139521504711E+07 plusOrMinus 5e-1)
            barycentric.position.a2 shouldBe (1.957005895707266E+08 plusOrMinus 5e-1)
            barycentric.position.a3 shouldBe (8.774122508349305E+07 plusOrMinus 5e-1)
        }
    }
}
