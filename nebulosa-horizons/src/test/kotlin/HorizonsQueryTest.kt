import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsQuery
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance.Companion.m
import java.time.LocalDateTime

class HorizonsQueryTest : StringSpec() {

    init {
        "observer" {
            val query = HorizonsQuery()
            val ephemeris = query.observer(
                "499",
                LocalDateTime.of(2022, 12, 25, 22, 0, 0),
                LocalDateTime.of(2022, 12, 25, 23, 0, 0),
                latitude = 35.36276754848444.deg, longitude = 138.73119026648095.deg,
                elevation = 3776.m,
            )

            val startTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[startTime]!![HorizonsQuantity.ASTROMETRIC_RA_DEC] shouldBe "67.876674963 24.657460871"
            ephemeris[startTime]!![HorizonsQuantity.CONSTELLATION] shouldBe "Tau"
            ephemeris[startTime]!![HorizonsQuantity.ILLUMINATED_FRACTION] shouldBe "98.32712"
            ephemeris[startTime]!![HorizonsQuantity.VISUAL_MAGNITUDE_SURFACE_BRIGHTNESS] shouldBe "-1.426 4.239"
            ephemeris[startTime]!![HorizonsQuantity.APPARENT_AZ_ALT] shouldBe "317.889826208 -16.319680741"
            ephemeris[startTime]!![HorizonsQuantity.APPARENT_HOUR_ANGLE] shouldBe "8.993056236"
        }
    }
}
