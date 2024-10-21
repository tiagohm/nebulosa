import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.test.download
import nebulosa.time.*
import org.junit.jupiter.api.Test
import kotlin.io.path.inputStream

class IERSTest {

    @Test
    fun iersa() {
        IERS.attach(IERSA)

        with(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-18)
            fraction shouldBe (-1.2784074073774842e-06 plusOrMinus 1E-18)
        }
        with(UTC(TimeYMDHMS(2026, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2461042.0 plusOrMinus 1E-18)
            fraction shouldBe (1.1488645833633137E-6 plusOrMinus 1E-18)
        }
        with(UTC(TimeYMDHMS(1964, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2438396.0 plusOrMinus 1E-18)
            fraction shouldBe (9.353564814864956e-06 plusOrMinus 1E-8)
        }
    }

    @Test
    fun iersb() {
        IERS.attach(IERSB)

        with(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-18)
            fraction shouldBe (-1.2782876157107722e-06 plusOrMinus 1E-18)
        }
        with(UTC(TimeYMDHMS(2026, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2461042.0 plusOrMinus 1E-18)
            fraction shouldBe (6.586666666966254E-7 plusOrMinus 1E-18)
        }
        with(UTC(TimeYMDHMS(1964, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2438396.0 plusOrMinus 1E-18)
            fraction shouldBe (-9.322685184683761e-07 plusOrMinus 1E-8)
        }
    }

    @Test
    fun iersab() {
        val iersab = IERSAB(IERSA, IERSB)
        IERS.attach(iersab)

        with(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-18)
            fraction shouldBe (-1.2782876157107722e-06 plusOrMinus 1E-18)
        }
        with(UTC(TimeYMDHMS(2026, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2461042.0 plusOrMinus 1E-18)
            fraction shouldBe (1.1488645833633137E-6 plusOrMinus 1E-18)
        }
        with(UTC(TimeYMDHMS(1964, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2438396.0 plusOrMinus 1E-18)
            fraction shouldBe (-9.322685184683761e-07 plusOrMinus 1E-8)
        }
    }

    companion object {

        @JvmStatic private val IERSA = IERSA()
        @JvmStatic private val IERSB = IERSB()

        init {
            val finals2000A = download("https://maia.usno.navy.mil/ser7/finals2000A.all")
            val eopc04 = download("https://hpiers.obspm.fr/iers/eop/eopc04/eopc04.1962-now")

            finals2000A.inputStream().use(IERSA::load)
            eopc04.inputStream().use(IERSB::load)
        }
    }
}
