import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.meeus.MoonPhase
import org.junit.jupiter.api.Test

// https://github.com/commenthol/astronomia/blob/master/test/moonphase.test.js

class MoonPhaseTest {

    @Test
    fun mean() {
        MoonPhase.mean(0.0) shouldBeExactly 0.0
        MoonPhase.mean(2443192.94102) shouldBeExactly 0.0
    }

    @Test
    fun meanNew() {
        MoonPhase.meanNew(1977.123) shouldBe (2443192.94102 plusOrMinus 1e-5)
    }
}
