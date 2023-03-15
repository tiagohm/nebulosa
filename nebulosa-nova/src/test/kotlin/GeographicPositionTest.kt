import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Distance.Companion.m
import nebulosa.nova.position.Geoid
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UT1

class GeographicPositionTest : StringSpec() {

    init {
        IERSA.load(resource("finals2000A.all")!!)
        IERS.attach(IERSA)

        "lst" {
            val latitude = Angle.from("-23 32 51.00")!!
            val longitude = Angle.from("-46 38 10.00")!!

            val position = Geoid.IERS2010.latLon(longitude, latitude)
            val (h, m, s) = position.lstAt(UT1(TimeYMDHMS(2023, 1, 30, 22))).hms()

            h.toInt() shouldBeExactly 3
            m.toInt() shouldBeExactly 32
            s shouldBe (57.6356 plusOrMinus 1e-2)
        }
        "xyz" {
            val latitude = Angle.from("-23 32 51.00")!!
            val longitude = Angle.from("-46 38 10.00")!!
            val position = Geoid.IERS2010.latLon(longitude, latitude, 853.0.m)

            position.x.value shouldBe (2.685480929038628E-5 plusOrMinus 1e-13)
            position.y.value shouldBe (-2.8434040742871705E-5 plusOrMinus 1e-13)
            position.z.value shouldBe (-1.693045603541487E-5 plusOrMinus 1e-13)
        }
    }
}
