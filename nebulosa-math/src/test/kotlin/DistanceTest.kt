import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.au
import nebulosa.math.km
import nebulosa.math.toKilometers
import nebulosa.math.toLightYears
import nebulosa.math.toMeters
import org.junit.jupiter.api.Test

class DistanceTest {

    @Test
    fun au() {
        1.au.toKilometers shouldBe (149597870.700 plusOrMinus 1e-8)
        1.au.toMeters shouldBe (149597870700.0 plusOrMinus 1e-8)
        1.au.toLightYears shouldBe (0.000015813 plusOrMinus 1e-8)
    }

    @Test
    fun km() {
        8000.km shouldBe (0.00005348 plusOrMinus 1e-8)
        8000.km.toMeters shouldBe (8000000.0 plusOrMinus 1e-8)
        8000.km.toLightYears shouldBe (0.0000000008456 plusOrMinus 1e-12)
    }

    @Test
    fun plus() {
        (0.5.au + 0.5.au) shouldBeExactly 1.0
        (0.5.au + 0.5) shouldBeExactly 1.0
    }

    @Test
    fun minus() {
        (0.8.au - 0.5.au) shouldBe (0.3 plusOrMinus 1e-2)
        (0.8.au - 0.5) shouldBe (0.3 plusOrMinus 1e-2)
    }

    @Test
    fun times() {
        (0.5.au * 5) shouldBeExactly 2.5
    }

    @Test
    fun div() {
        (5.0.au / 5) shouldBeExactly 1.0
        5.0.au / 5.0.au shouldBeExactly 1.0
    }

    @Test
    fun rem() {
        (5.0.au % 5) shouldBeExactly 0.0
        (5.0.au % 5.0.au) shouldBeExactly 0.0
    }
}
