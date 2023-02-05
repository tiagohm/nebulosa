import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle
import nebulosa.time.TimeYMDHMS

class InstantOfTimeTest : StringSpec() {

    init {
        // https://dc.zah.uni-heidelberg.de/apfs/times/q/form

        "gast" {
            val angle = Angle.from("06 39 30.2996", isHours = true)!!
            TimeYMDHMS(2023, 1, 30, 22).gast.hours shouldBe (angle.hours plusOrMinus 1e-8)
        }
        "gmst" {
            val angle = Angle.from("06 39 30.8663", isHours = true)!!
            TimeYMDHMS(2023, 1, 30, 22).gmst.hours shouldBe (angle.hours plusOrMinus 1e-8)
        }
        "era" {
            val angle = Angle.from("99 34 58.365")!!
            TimeYMDHMS(2023, 1, 30, 22).era.degrees shouldBe (angle.degrees plusOrMinus 1e-6)
        }
    }
}
