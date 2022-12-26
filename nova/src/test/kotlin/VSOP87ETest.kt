import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.time.TDB
import nebulosa.time.TimeJD

class VSOP87ETest : StringSpec() {

    init {
        "sun" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.SUN.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (-9.061436632282236E-03 plusOrMinus 1e-5)
            p[1] shouldBe (6.152584183942633E-05 plusOrMinus 1e-6)
            p[2] shouldBe (2.553315997818977E-04 plusOrMinus 1e-6)
            v[0] shouldBe (8.523732347654815E-07 plusOrMinus 1e-9)
            v[1] shouldBe (-8.293160642939413E-06 plusOrMinus 1e-9)
            v[2] shouldBe (-3.536408207768117E-06 plusOrMinus 1e-9)
        }
        "mercury" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.MERCURY.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (2.917749477051785E-01 plusOrMinus 1e-5)
            p[1] shouldBe (1.307753573063683E-01 plusOrMinus 1e-5)
            p[2] shouldBe (3.890188068703725E-02 plusOrMinus 1e-5)
            v[0] shouldBe (-1.701508873456466E-02 plusOrMinus 1e-8)
            v[1] shouldBe (2.317979796647154E-02 plusOrMinus 1e-8)
            v[2] shouldBe (1.414717965470348E-02 plusOrMinus 1e-8)
        }
        "venus" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.VENUS.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (4.530231016612877E-01 plusOrMinus 1e-5)
            p[1] shouldBe (-5.016891998928698E-01 plusOrMinus 1e-5)
            p[2] shouldBe (-2.547479296682298E-01 plusOrMinus 1e-5)
            v[0] shouldBe (1.548765426376424E-02 plusOrMinus 1e-7)
            v[1] shouldBe (1.199990886845585E-02 plusOrMinus 1e-7)
            v[2] shouldBe (4.419843673733974E-03 plusOrMinus 1e-7)
        }
        "earth" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.EARTH.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (-5.774546457978428E-02 plusOrMinus 1e-5)
            p[1] shouldBe (9.014209322230198E-01 plusOrMinus 1e-5)
            p[2] shouldBe (3.909900074777803E-01 plusOrMinus 1e-5)
            v[0] shouldBe (-1.746880754444190E-02 plusOrMinus 1e-7)
            v[1] shouldBe (-8.523045001854702E-04 plusOrMinus 1e-7)
            v[2] shouldBe (-3.690873763513220E-04 plusOrMinus 1e-7)
        }
        "mars" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.MARS.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (1.458124773200954E-01 plusOrMinus 1e-5)
            p[1] shouldBe (1.408612236938829E+00 plusOrMinus 1e-5)
            p[2] shouldBe (6.421493185701298E-01 plusOrMinus 1e-5)
            v[0] shouldBe (-1.339320293960258E-02 plusOrMinus 1e-7)
            v[1] shouldBe (2.208863862273841E-03 plusOrMinus 1e-7)
            v[2] shouldBe (1.374808996076603E-03 plusOrMinus 1e-7)
        }
        "jupiter" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.JUPITER.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (4.840655361543647E+00 plusOrMinus 1e-5)
            p[1] shouldBe (9.540894823613197E-01 plusOrMinus 1e-5)
            p[2] shouldBe (2.911277391212874E-01 plusOrMinus 1e-5)
            v[0] shouldBe (-1.599598915586027E-03 plusOrMinus 1e-7)
            v[1] shouldBe (7.106125606377319E-03 plusOrMinus 1e-6)
            v[2] shouldBe (3.084863409738924E-03 plusOrMinus 1e-7)
        }
        "saturn" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.SATURN.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (8.118575220877252E+00 plusOrMinus 1e-5)
            p[1] shouldBe (-4.990053770638640E+00 plusOrMinus 1e-5)
            p[2] shouldBe (-2.410825392716150E+00 plusOrMinus 1e-5)
            v[0] shouldBe (2.830471313637587E-03 plusOrMinus 1e-6)
            v[1] shouldBe (4.295334526830042E-03 plusOrMinus 1e-6)
            v[2] shouldBe (1.652340036885691E-03 plusOrMinus 1e-6)
        }
        "uranus" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.URANUS.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (1.338107802611238E+01  plusOrMinus 1e-4)
            p[1] shouldBe (1.326832905623722E+01 plusOrMinus 1e-4)
            p[2] shouldBe (5.621910507435053E+00 plusOrMinus 1e-4)
            v[0] shouldBe (-2.910912382282302E-03 plusOrMinus 1e-7)
            v[1] shouldBe (2.268725951577151E-03 plusOrMinus 1e-7)
            v[2] shouldBe (1.034738977414487E-03 plusOrMinus 1e-7)
        }
        "neptune" {
            val time = TDB(TimeJD(2459938.0, 0.5))
            val (p, v) = VSOP87E.NEPTUNE.compute(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            p[0] shouldBe (2.974978125776706E+01 plusOrMinus 1e-4)
            p[1] shouldBe (-2.471636747869995E+00 plusOrMinus 1e-3)
            p[2] shouldBe (-1.752319118165945E+00 plusOrMinus 1e-4)
            v[0] shouldBe (2.908617762745452E-04 plusOrMinus 1e-6)
            v[1] shouldBe (2.911615062634410E-03 plusOrMinus 1e-6)
            v[2] shouldBe (1.184401587981311E-03 plusOrMinus 1e-6)
        }
    }
}
