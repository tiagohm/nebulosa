import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.time.TDB
import nebulosa.time.TDBMinusTTByFairheadAndBretagnon1990
import nebulosa.time.TimeDelta
import org.junit.jupiter.api.Test

class TDBMinusTTByFairheadAndBretagnon1990Test : TimeDelta by TDBMinusTTByFairheadAndBretagnon1990 {

    @Test
    fun delta() {
        delta(TDB(2440423.345833333)) shouldBe (-0.00046798717637519603 plusOrMinus 1e-16)
        delta(TDB(2448031.5)) shouldBe (0.0011585185926349208 plusOrMinus 1e-16)
        delta(TDB(2451545.0)) shouldBe (-9.575743486095212e-05 plusOrMinus 1e-16)
        delta(TDB(2456164.5)) shouldBe (-0.001241030165936046 plusOrMinus 1e-16)
    }
}
