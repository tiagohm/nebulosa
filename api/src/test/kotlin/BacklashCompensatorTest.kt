import io.kotest.matchers.shouldBe
import nebulosa.api.focusers.BacklashCompensation
import nebulosa.api.focusers.BacklashCompensationMode
import nebulosa.api.focusers.BacklashCompensator
import org.junit.jupiter.api.Test

class BacklashCompensatorTest {

    @Test
    fun absoluteIn() {
        val compensation = BacklashCompensation(BacklashCompensationMode.ABSOLUTE, 100, 0)
        val compensator = BacklashCompensator(compensation, 10000)

        compensator.compute(1000, 0) shouldBe intArrayOf(1000)
        compensator.compute(100, 1000) shouldBe intArrayOf(0)
    }

    @Test
    fun absoluteOut() {
        val compensation = BacklashCompensation(BacklashCompensationMode.ABSOLUTE, 0, 100)
        val compensator = BacklashCompensator(compensation, 10000)

        compensator.compute(1000, 0) shouldBe intArrayOf(1000)
        compensator.compute(0, 1000) shouldBe intArrayOf(0)
        compensator.compute(1000, 0) shouldBe intArrayOf(1100)
        compensator.compute(0, 1100) shouldBe intArrayOf(100)
    }

    @Test
    fun overshootIn() {
        val compensation = BacklashCompensation(BacklashCompensationMode.OVERSHOOT, 100, 0)
        val compensator = BacklashCompensator(compensation, 10000)

        compensator.compute(1000, 0) shouldBe intArrayOf(1000)
        compensator.compute(100, 1000) shouldBe intArrayOf(0, 100)
        compensator.compute(1000, 0) shouldBe intArrayOf(1000)
        compensator.compute(0, 1000) shouldBe intArrayOf(0)
    }

    @Test
    fun overshootOut() {
        val compensation = BacklashCompensation(BacklashCompensationMode.OVERSHOOT, 0, 100)
        val compensator = BacklashCompensator(compensation, 10000)

        compensator.compute(1000, 0) shouldBe intArrayOf(1100, 1000)
        compensator.compute(0, 1000) shouldBe intArrayOf(0)
        compensator.compute(1000, 0) shouldBe intArrayOf(1100, 1000)
        compensator.compute(0, 1000) shouldBe intArrayOf(0)
    }
}
