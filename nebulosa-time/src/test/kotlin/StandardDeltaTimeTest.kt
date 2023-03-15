import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.time.DeltaTime
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import kotlin.math.abs

class StandardDeltaTimeTest : StringSpec() {

    init {
        IERSA.load(resource("finals2000A.all")!!)

        "delta" {
            DeltaTime.Standard.delta(TimeYMDHMS(-720)) shouldBe (20370.94276516515 plusOrMinus 1e-4)
            DeltaTime.Standard.delta(TimeYMDHMS(1170)) shouldBe (997.1912592573422 plusOrMinus 1e-4)
            DeltaTime.Standard.delta(TimeYMDHMS(1980)) shouldBe (50.5387068 plusOrMinus 1e-4)
            DeltaTime.Standard.delta(TimeYMDHMS(2023)) shouldBe (69.203827 plusOrMinus 1e-4)
            DeltaTime.Standard.delta(TimeYMDHMS(2600)) shouldBe (1621.96383159486 plusOrMinus 1e-4)
        }
        "compared with iers" {
            var diff = 0.0

            for (i in 1800..2300) {
                val time = TimeYMDHMS(i)
                val dt0 = DeltaTime.Standard.delta(time)
                val dt1 = IERSA.delta(time)
                dt0 shouldBe (dt1 plusOrMinus 1e-4)
                diff += abs(dt1 - dt0)
            }

            (diff / 501) shouldBeLessThan 1e-7
        }
    }
}
