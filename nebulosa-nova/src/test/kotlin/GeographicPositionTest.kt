import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.deg
import nebulosa.math.formatHMS
import nebulosa.math.m
import nebulosa.nova.position.Geoid
import nebulosa.test.download
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.inputStream

class GeographicPositionTest {

    @Test
    fun lst() {
        val position = Geoid.IERS2010.lonLat((-45.4227).deg, 0.0)
        position.lstAt(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))).formatHMS() shouldBe "15h42m47.1s"
        position.lstAt(UTC(TimeYMDHMS(2024, 1, 1, 12, 0, 0.0))).formatHMS() shouldBe "15h40m53.1s"
        position.lstAt(UTC(TimeYMDHMS(2025, 1, 1, 12, 0, 0.0))).formatHMS() shouldBe "15h43m52.8s"
    }

    @Test
    fun xyz() {
        val latitude = "-23 32 51.00".deg
        val longitude = "-46 38 10.00".deg
        val position = Geoid.IERS2010.lonLat(longitude, latitude, 853.0.m)

        position.x shouldBe (-2.8434040742871705E-5 plusOrMinus 1e-13)
        position.y shouldBe (2.685480929038628E-5 plusOrMinus 1e-13)
        position.z shouldBe (-1.693045603541487E-5 plusOrMinus 1e-13)
    }

    companion object {

        @JvmStatic
        @BeforeAll
        fun loadIERS() {
            val iersa = IERSA()
            val finals2000A = download("https://github.com/tiagohm/nebulosa.data/raw/main/finals2000A.all.txt")
            finals2000A.inputStream().use(iersa::load)
            IERS.attach(iersa)
        }
    }
}
