import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.*
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.inputStream
import kotlin.math.truncate

class FixedStarTest {

    @Test
    fun polaris() {
        // https://api.noctuasky.com/api/v1/skysources/name/NAME%20Polaris
        val star = FixedStar(
            37.95456067.deg, 89.26410897.deg,
            (44.48).mas, (-11.85).mas, (7.54).mas, (-16.42).kms,
        )

        val astrometric = VSOP87E.EARTH.at<Barycentric>(UTC(TimeYMDHMS(2024, 2, 17, 12, 0, 0.0)))
            .observe(star)

        val (ra, dec) = astrometric.equatorialAtDate()

        with(ra.normalized.hms()) {
            truncate(this[0]) shouldBeExactly 3.0
            truncate(this[1]) shouldBeExactly 2.0
            this[2] shouldBe (3.9 plusOrMinus 20.0)
        }

        with(dec.dms()) {
            truncate(this[0]) shouldBeExactly 89.0
            truncate(this[1]) shouldBe (22.0 plusOrMinus 1.0)
            this[2] shouldBe (15.8 plusOrMinus 50.0)
        }
    }

    @Test
    fun barnardsStar() {
        // https://api.noctuasky.com/api/v1/skysources/name/NAME%20Barnard's%20Star
        val star = FixedStar(
            269.452082497514.deg, 4.6933642650633.deg,
            (-802.803).mas, (10362.542).mas, (547.451).mas, (-110.51).kms,
        )

        val astrometric = VSOP87E.EARTH.at<Barycentric>(UTC(TimeYMDHMS(2024, 2, 17, 12, 0, 0.0)))
            .observe(star)

        val (ra, dec) = astrometric.equatorialAtDate()

        with(ra.normalized.hms()) {
            truncate(this[0]) shouldBeExactly 17.0
            truncate(this[1]) shouldBeExactly 58.0
            this[2] shouldBe (57.8 plusOrMinus 1.0)
        }

        with(dec.dms()) {
            truncate(this[0]) shouldBeExactly 4.0
            truncate(this[1]) shouldBeExactly 45.0
            this[2] shouldBe (25.5 plusOrMinus 10.0)
        }
    }

    companion object {

        @JvmStatic private val IERSA = IERSA()

        @JvmStatic
        @BeforeAll
        fun loadIERS() {
            dataDirectory.concat("finals2000A.all").inputStream().use(IERSA::load)
            IERS.attach(IERSA)
        }
    }
}
