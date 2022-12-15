import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nova.astrometry.Asteroid
import nebulosa.nova.astrometry.ICRF
import nebulosa.time.TT
import nebulosa.time.TimeYMDHMS

class KeplerOrbitTest : StringSpec() {

    init {
        "asteroid" {
            val asteroid = Asteroid.parse("00001    3.33  0.15 K232P  17.21569   73.47045   80.26013   10.58634  0.0788175  0.21411523   2.7671817  0 MPO719049  7258 123 1801-2022 0.65 M-v 30l MPCLINUX   0000      (1) Ceres              20220916")
            val time = TT(TimeYMDHMS(2022, 12, 15, 22, 0, 0.0))
            val icrf = asteroid.at<ICRF>(time)
            // https://ssd.jpl.nasa.gov/horizons/app.html#/
            // 1 Ceres (A801 AA) <-> Sun (body center) [500@10]
            icrf.position[0] shouldBe (-2.288751686186975E+00 plusOrMinus 1e-2)
            icrf.position[1] shouldBe (7.613620144485219E-01 plusOrMinus 1e-2)
            icrf.position[2] shouldBe (8.249858620145536E-01 plusOrMinus 1e-2)
            icrf.velocity[0] shouldBe (-4.521640529931040E-03 plusOrMinus 1e-2)
            icrf.velocity[1] shouldBe (-9.582114691782662E-03 plusOrMinus 1e-2)
            icrf.velocity[2] shouldBe (-3.598744790228161E-03 plusOrMinus 1e-2)
        }
    }
}
