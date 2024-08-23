import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.meeus.Base
import org.junit.jupiter.api.Test

// https://github.com/commenthol/astronomia/blob/master/test/base.test.js

class BaseTest {

    @Test
    fun horner() {
        // 2x³-6x²+2x-1 at x=3
        Base.horner(3.0, -1.0, 2.0, -6.0, 2.0) shouldBeExactly 5.0
    }
}
