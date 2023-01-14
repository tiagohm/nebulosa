import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
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
            val position = Geoid.IERS2010.latLon((-45.0).deg, Angle.ZERO)
            position.lstAt(UTC(TimeYMDHMS(1994, 1, 28, 22))).hours shouldBeExactly 6.46 - 3.0
        }
    }
}
