import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle
import nebulosa.math.Distance.Companion.m
import nebulosa.nova.position.Geoid
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import java.io.File

class GeographicPositionTest : StringSpec() {

    init {
        IERSA.load(File("../assets/finals2000A.all").inputStream())
        IERS.current = IERSA

        "lst" {
            val latitude = Angle.from("-23 32 51.00")!!
            val longitude = Angle.from("-46 38 9.99")!!

            val position = Geoid.IERS2010.latLon(longitude, latitude)
            val (h, m, s) = position.lstAt(UTC(TimeYMDHMS(2023, 1, 30, 22))).hms()

            h.toInt() shouldBeExactly 3
            m.toInt() shouldBeExactly 38
            s.toInt() shouldBeExactly 2
        }
        "xyz" {
            val latitude = Angle.from("-23 32 51.00")!!
            val longitude = Angle.from("-46 38 9.99")!!
            val position = Geoid.IERS2010.latLon(longitude, latitude, 853.0.m)

            position.x.value shouldBe (2.77020594e-05 plusOrMinus 1e-13)
            position.y.value shouldBe (-2.80561908e-05 plusOrMinus 1e-13)
            position.z.value shouldBe (-1.61842018e-05 plusOrMinus 1e-13)
        }
    }
}
