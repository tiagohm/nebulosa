import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nova.astrometry.ICRF
import nebulosa.nova.astrometry.VSOP87A
import nebulosa.nova.position.Geocentric
import nebulosa.time.TDB
import nebulosa.time.TimeJD

// http://www.astrosurf.com/jephem/astro/ephemeris/et380Ztest_complete_full.htm

class VSOP87ATest : StringSpec() {

    init {
        "mercury - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Mercury.at<ICRF>(time)
            barycentric.position[0] shouldBe (-0.1300934115 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (-0.4472876716 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.0245983802 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (0.0213663982 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (-0.0064479797 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (-0.0024878668 plusOrMinus 1e-10)
        }
        "venus - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Venus.at<ICRF>(time)
            barycentric.position[0] shouldBe (-0.7183022797 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (-0.0326546017 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (0.0410142975 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (0.0007981261 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (-0.0202952188 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (-0.0003234551 plusOrMinus 1e-10)
        }
        "earth - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Earth.at<ICRF>(time)
            barycentric.position[0] shouldBe (-0.1771354586 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (0.9672416237 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.0000039000 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (-0.0172076240 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (-0.0031587881 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (0.0000001069 plusOrMinus 1e-10)
        }
        "mars - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Mars.at<ICRF>(time)
            barycentric.position[0] shouldBe (1.3907159264 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (-0.0134157043 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.0344677967 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (0.0006714930 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (0.0151872479 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (0.0003016546 plusOrMinus 1e-10)
        }
        "jupiter - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Jupiter.at<ICRF>(time)
            barycentric.position[0] shouldBe (4.0011740268 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (2.9385810077 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.1017837501 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (-0.0045683226 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (0.0064432013 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (0.0000755806 plusOrMinus 1e-10)
        }
        "saturn - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Saturn.at<ICRF>(time)
            barycentric.position[0] shouldBe (6.4064068573 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (6.5699929449 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.3690768029 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (-0.0042923542 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (0.0038903162 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (0.0001029504 plusOrMinus 1e-10)
        }
        "uranus - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Uranus.at<ICRF>(time)
            barycentric.position[0] shouldBe (14.4318934159 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (-13.7343162527 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.2381421963 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (0.0026781013 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (0.0026726895 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (-0.0000247716 plusOrMinus 1e-10)
        }
        "neptune - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.Neptune.at<ICRF>(time)
            barycentric.position[0] shouldBe (16.8121116576 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (-24.9916630908 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (0.1272190171 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (0.0025792564 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (0.0017769299 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (-0.0000959082 plusOrMinus 1e-10)
        }
        "EMB - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val barycentric = VSOP87A.EarthMoonBarycenter.at<ICRF>(time)
            barycentric.position[0] shouldBe (-0.1771591440 plusOrMinus 1e-10)
            barycentric.position[1] shouldBe (0.9672192891 plusOrMinus 1e-10)
            barycentric.position[2] shouldBe (-0.0000009536 plusOrMinus 1e-10)
            barycentric.velocity[0] shouldBe (-0.0172031075 plusOrMinus 1e-10)
            barycentric.velocity[1] shouldBe (-0.0031639188 plusOrMinus 1e-10)
            barycentric.velocity[2] shouldBe (0.0000000258 plusOrMinus 1e-10)
        }
        "moon - 01-01-2000 12:00:00 TDB" {
            val time = TDB(TimeJD.J2000)
            val geocentric = VSOP87A.Moon.at<Geocentric>(time)
            geocentric.center shouldBe 399
            geocentric.target shouldBe 301
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            geocentric.position[0] shouldBe (-1.949281649686695E-03 plusOrMinus 1e-7)
            geocentric.position[1] shouldBe (-1.782891912873099E-03 plusOrMinus 1e-4)
            geocentric.position[2] shouldBe (-5.087137066222156E-04 plusOrMinus 1e-4)
            geocentric.velocity[0] shouldBe (3.716704773167968E-04 plusOrMinus 1e-10)
            geocentric.velocity[1] shouldBe (-3.846978271692960E-04 plusOrMinus 1e-10)
            geocentric.velocity[2] shouldBe (-1.740301567187911E-04 plusOrMinus 1e-10)
        }
    }
}
