import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nova.astrometry.GUST86
import nebulosa.time.TDB
import nebulosa.time.TimeJD

class GUST86Test : StringSpec() {

    init {
        "ariel" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = GUST86.ARIEL.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (-4.451100762924695E-04 plusOrMinus 1e-5)
            p[1] shouldBe (4.127293811714091E-04 plusOrMinus 1e-5)
            p[2] shouldBe (-1.122529644334287E-03 plusOrMinus 1e-5)
            v[0] shouldBe (-2.905800702342619E-03 plusOrMinus 1e-5)
            v[1] shouldBe (3.018674943703828E-04 plusOrMinus 1e-5)
            v[2] shouldBe (1.261178539831990E-03 plusOrMinus 1e-5)
        }
        "umbriel" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = GUST86.UMBRIEL.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (-1.611384907091204E-03 plusOrMinus 1e-5)
            p[1] shouldBe (1.606295364496704E-04 plusOrMinus 1e-5)
            p[2] shouldBe (7.209771384443374E-04 plusOrMinus 1e-5)
            v[0] shouldBe (9.751794785461074E-04 plusOrMinus 1e-5)
            v[1] shouldBe (-8.767828036553259E-04 plusOrMinus 1e-5)
            v[2] shouldBe (2.364500180196773E-03 plusOrMinus 1e-5)
        }
        "titania" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = GUST86.TITANIA.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (-2.232644660544111E-03 plusOrMinus 1e-5)
            p[1] shouldBe (9.515348359690534E-04 plusOrMinus 1e-5)
            p[2] shouldBe (-1.618326368294999E-03 plusOrMinus 1e-5)
            v[0] shouldBe (-1.281512705822040E-03 plusOrMinus 1e-5)
            v[1] shouldBe (-1.711924613002539E-04 plusOrMinus 1e-5)
            v[2] shouldBe (1.660414235776199E-03 plusOrMinus 1e-5)
        }
        "oberon" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = GUST86.OBERON.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (3.195547478166968E-03 plusOrMinus 1e-5)
            p[1] shouldBe (-1.226712898705434E-03 plusOrMinus 1e-5)
            p[2] shouldBe (1.866878239761929E-03 plusOrMinus 1e-5)
            v[0] shouldBe (9.686636758969398E-04 plusOrMinus 1e-5)
            v[1] shouldBe (2.027942751407152E-04 plusOrMinus 1e-5)
            v[2] shouldBe (-1.527974319936715E-03 plusOrMinus 1e-5)
        }
        "miranda" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = GUST86.MIRANDA.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (-7.240222342711871E-04 plusOrMinus 1e-5)
            p[1] shouldBe (7.574953585547300E-05 plusOrMinus 1e-5)
            p[2] shouldBe (4.707549340071754E-04 plusOrMinus 1e-5)
            v[0] shouldBe (1.941801978392934E-03 plusOrMinus 1e-5)
            v[1] shouldBe (-1.068242211440700E-03 plusOrMinus 1e-5)
            v[2] shouldBe (3.164065185998637E-03 plusOrMinus 1e-5)
        }
    }
}
