import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.auDay
import nebulosa.math.kms
import nebulosa.math.ms
import nebulosa.math.toKilometersPerSecond
import nebulosa.math.toMetersPerSecond
import org.junit.jupiter.api.Test

class VelocityTest {

    @Test
    fun auDay() {
        1.auDay.toMetersPerSecond shouldBe (1731456.8368055555 plusOrMinus 1e-8)
        1.auDay.toKilometersPerSecond shouldBe (1731.4568368055554 plusOrMinus 1e-8)
    }

    @Test
    fun kms() {
        8000.kms shouldBe (4.62038661891195 plusOrMinus 1e-8)
        8000.kms.toMetersPerSecond shouldBe (8000000.0 plusOrMinus 1e-8)
    }

    @Test
    fun ms() {
        8000.ms shouldBe (0.00462038661891195 plusOrMinus 1e-8)
        8000.ms.toKilometersPerSecond shouldBe (8.0 plusOrMinus 1e-12)
    }

    @Test
    fun plus() {
        (0.5.auDay + 0.5.auDay) shouldBeExactly 1.0
        (0.5.auDay + 0.5) shouldBeExactly 1.0
    }

    @Test
    fun minus() {
        (0.8.auDay - 0.5.auDay) shouldBe (0.3 plusOrMinus 1e-2)
        (0.8.auDay - 0.5) shouldBe (0.3 plusOrMinus 1e-2)
    }

    @Test
    fun times() {
        (0.5.auDay * 5) shouldBeExactly 2.5
    }

    @Test
    fun div() {
        (5.0.auDay / 5) shouldBeExactly 1.0
        5.0.auDay / 5.0.auDay shouldBeExactly 1.0
    }

    @Test
    fun rem() {
        (5.0.auDay % 5) shouldBeExactly 0.0
        (5.0.auDay % 5.0.auDay) shouldBeExactly 0.0
    }
}
