import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.time.DeltaT
import nebulosa.time.TimeDelta
import nebulosa.time.TimeYMDHMS
import org.junit.jupiter.api.Test

class DeltaTTest : TimeDelta by DeltaT {

    @Test
    fun delta() {
        delta(TimeYMDHMS(-720)) shouldBe (20370.94276516515 plusOrMinus 1e-4)
        delta(TimeYMDHMS(1170)) shouldBe (997.1912592573422 plusOrMinus 1e-4)
        delta(TimeYMDHMS(1980)) shouldBe (50.53915198016422 plusOrMinus 1e-4)
        delta(TimeYMDHMS(2023)) shouldBe (69.203827 plusOrMinus 1e-4)
        delta(TimeYMDHMS(2600)) shouldBe (1623.7769924989768 plusOrMinus 1e-4)
    }
}
