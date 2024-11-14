import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.test.download
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.IERSAB
import nebulosa.time.IERSB
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.Test
import kotlin.io.path.inputStream

class IERSTest {

    @Test
    fun iersa() {
        IERS.attach(IERSA)

        with(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-8)
            fraction shouldBe (-1.2784074073774842e-06 plusOrMinus 1E-8)
        }
        with(UTC(TimeYMDHMS(2026, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2461042.0 plusOrMinus 1E-8)
            fraction shouldBe (1.111424768548443E-6 plusOrMinus 1E-8)
        }
        with(UTC(TimeYMDHMS(1964, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2438396.0 plusOrMinus 1E-8)
            fraction shouldBe (9.353564814864956e-06 plusOrMinus 1E-8)
        }
    }

    @Test
    fun iersb() {
        IERS.attach(IERSB)

        with(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-8)
            fraction shouldBe (-1.2782876157107722E-6 plusOrMinus 1E-8)
        }
        with(UTC(TimeYMDHMS(2026, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2461042.0 plusOrMinus 1E-8)
            fraction shouldBe (6.832025463261943E-7 plusOrMinus 1E-8)
        }
        with(UTC(TimeYMDHMS(1964, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2438396.0 plusOrMinus 1E-8)
            fraction shouldBe (-9.322685184683761e-07 plusOrMinus 1E-8)
        }
    }

    @Test
    fun iersab() {
        val iersab = IERSAB(IERSA, IERSB)
        IERS.attach(iersab)

        with(UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-8)
            fraction shouldBe (-1.2782876157107722e-06 plusOrMinus 1E-8)
        }
        with(UTC(TimeYMDHMS(2026, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2461042.0 plusOrMinus 1E-8)
            fraction shouldBe (1.111424768548443E-6 plusOrMinus 1E-8)
        }
        with(UTC(TimeYMDHMS(1964, 1, 1, 12, 0, 0.0)).ut1) {
            whole shouldBe (2438396.0 plusOrMinus 1E-8)
            fraction shouldBe (-9.322685184683761e-07 plusOrMinus 1E-8)
        }
    }

    companion object {

        private val IERSA = IERSA()
        private val IERSB = IERSB()

        init {
            val finals2000A = download("https://github.com/tiagohm/nebulosa.data/raw/main/finals2000A.all.txt")
            val eopc04 = download("https://github.com/tiagohm/nebulosa.data/raw/main/eopc04.1962-now.txt")

            finals2000A.inputStream().use(IERSA::load)
            eopc04.inputStream().use(IERSB::load)
        }
    }
}
