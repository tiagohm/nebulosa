import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.Velocity.Companion.auDay
import nebulosa.math.Velocity.Companion.kms
import nebulosa.math.Velocity.Companion.ms

class VelocityTest : StringSpec() {

    init {

        timeout = 1000L

        "au/day" {
            1.auDay.ms shouldBe (1731456.8368055555 plusOrMinus 1e-8)
            1.auDay.kms shouldBe (1731.4568368055554 plusOrMinus 1e-8)
        }
        "km/s" {
            8000.kms.value shouldBe (4.62038661891195 plusOrMinus 1e-8)
            8000.kms.ms shouldBe (8000000.0 plusOrMinus 1e-8)
        }
        "m/s" {
            8000.ms.value shouldBe (0.00462038661891195 plusOrMinus 1e-8)
            8000.ms.kms shouldBe (8.0 plusOrMinus 1e-12)
        }
        "plus" {
            (0.5.auDay + 0.5.auDay).value shouldBeExactly 1.0
            (0.5.auDay + 0.5).value shouldBeExactly 1.0
        }
        "minus" {
            (0.8.auDay - 0.5.auDay).value shouldBe (0.3 plusOrMinus 1e-2)
            (0.8.auDay - 0.5).value shouldBe (0.3 plusOrMinus 1e-2)
        }
        "times" {
            (0.5.auDay * 5).value shouldBeExactly 2.5
        }
        "div" {
            (5.0.auDay / 5).value shouldBeExactly 1.0
            5.0.auDay / 5.0.auDay shouldBeExactly 1.0
        }
        "rem" {
            (5.0.auDay % 5).value shouldBeExactly 0.0
            (5.0.auDay % 5.0.auDay).value shouldBeExactly 0.0
        }
    }
}
