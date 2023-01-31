import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
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
            val latitude = Angle.parseCoordinatesAsDouble("-23 32 51.00").deg
            val longitude = Angle.parseCoordinatesAsDouble("-46 38 9.99").deg

            val position = Geoid.IERS2010.latLon(longitude, latitude)
            val (h, m, s) = position.lstAt(UTC(TimeYMDHMS(2023, 1, 30, 22))).hms()

            h.toInt() shouldBeExactly 3
            m.toInt() shouldBeExactly 32
            s.toInt() shouldBeExactly 57
        }
        "xyz" {
            val latitude = Angle.parseCoordinatesAsDouble("-23 32 51.00").deg
            val longitude = Angle.parseCoordinatesAsDouble("-46 38 9.99").deg
            val position = Geoid.IERS2010.latLon(longitude, latitude, 853.0.m)

            position.x shouldBe (2.68548107e-05 plusOrMinus 1e-13)
            position.y shouldBe (-2.84340394e-05 plusOrMinus 1e-13)
            position.z shouldBe (-1.69304560e-05 plusOrMinus 1e-13)
        }
    }
}
