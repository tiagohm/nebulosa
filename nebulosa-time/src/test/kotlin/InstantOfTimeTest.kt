import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import nebulosa.time.TimeYMDHMS

class InstantOfTimeTest : StringSpec() {

    init {
        // https://dc.zah.uni-heidelberg.de/apfs/times/q/form

        "gast" {
            val angle = "06 39 30.2996".hours
            TimeYMDHMS(2023, 1, 30, 22).gast.toHours shouldBe (angle.toHours plusOrMinus 1e-8)
        }
        "gmst" {
            val angle = "06 39 30.8663".hours
            TimeYMDHMS(2023, 1, 30, 22).gmst.toHours shouldBe (angle.toHours plusOrMinus 1e-8)
        }
        "era" {
            val angle = "99 34 58.365".deg
            TimeYMDHMS(2023, 1, 30, 22).era.toDegrees shouldBe (angle.toDegrees plusOrMinus 1e-6)
        }
        "as datetime" {
            var ymdhms = TimeYMDHMS(2023, 1, 30, 22, 45, 33.5)
            var datetime = ymdhms.asDateTime()
            datetime.year shouldBeExactly 2023
            datetime.monthValue shouldBeExactly 1
            datetime.dayOfMonth shouldBeExactly 30
            datetime.hour shouldBeExactly 22
            datetime.minute shouldBeExactly 45
            datetime.second shouldBeExactly 33
            datetime.nano shouldBeExactly 500000000

            ymdhms = TimeYMDHMS(2023, 1, 30, 0, 0, 0.0)
            datetime = ymdhms.asDateTime()
            datetime.year shouldBeExactly 2023
            datetime.monthValue shouldBeExactly 1
            datetime.dayOfMonth shouldBeExactly 30
            datetime.hour shouldBeExactly 0
            datetime.minute shouldBeExactly 0
            datetime.second shouldBeExactly 0
            datetime.nano shouldBeExactly 0
        }
    }
}
