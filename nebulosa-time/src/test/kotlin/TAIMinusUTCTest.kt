import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.time.TAIMinusUTC
import nebulosa.time.TimeDelta
import nebulosa.time.TimeYMDHMS

class TAIMinusUTCTest : StringSpec(), TimeDelta by TAIMinusUTC {

    init {
        "delta" {
            delta(TimeYMDHMS(2003, 6, 1)) shouldBeExactly 32.0
            delta(TimeYMDHMS(2008, 1, 17)) shouldBeExactly 33.0
            delta(TimeYMDHMS(2017, 9, 1)) shouldBeExactly 37.0
            delta(TimeYMDHMS(2026, 1, 1)) shouldBeExactly 37.0
        }
    }
}
