import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.toKilometers
import nebulosa.math.toKilometersPerSecond
import nebulosa.nova.astrometry.ELPMPP02
import nebulosa.time.TDB
import nebulosa.time.TimeJD
import org.junit.jupiter.api.Test

class ELPMPP02Test {

    @Test
    fun moon() {
        val (p, v) = ELPMPP02.compute(TIME)
        // https://ssd.jpl.nasa.gov/horizons/app.html#/ -> Moon, Geocentric, Start=2022-12-25 00:00:00 TDB, x-y axes
        p[0].toKilometers shouldBe (1.515958827661175E+05 plusOrMinus 1e-1)
        p[1].toKilometers shouldBe (-2.847574091094912E+05 plusOrMinus 1e-1)
        p[2].toKilometers shouldBe (-1.567484113713706E+05 plusOrMinus 1e-1)
        v[0].toKilometersPerSecond shouldBe (9.977921930078181E-01 plusOrMinus 1e-4)
        v[1].toKilometersPerSecond shouldBe (4.288607799397314E-01 plusOrMinus 1e-4)
        v[2].toKilometersPerSecond shouldBe (1.541399975077169E-01 plusOrMinus 1e-4)
    }

    companion object {

        private val TIME = TDB(TimeJD(2459938.0, 0.5))
    }
}
