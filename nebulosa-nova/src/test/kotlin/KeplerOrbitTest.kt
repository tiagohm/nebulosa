import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Distance.Companion.m
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.Asteroid
import nebulosa.nova.position.ICRF
import nebulosa.time.TDB
import nebulosa.time.TimeYMDHMS

class KeplerOrbitTest : StringSpec() {

    init {
        "asteroid: MPC" {
            val ceres =
                Asteroid.parse("00001    3.33  0.15 K232P  17.21569   73.47045   80.26013   10.58634  0.0788175  0.21411523   2.7671817  0 MPO719049  7258 123 1801-2022 0.65 M-v 30l MPCLINUX   0000      (1) Ceres              20220916")
            val time = TDB(TimeYMDHMS(2022, 12, 15, 22, 0, 0.0))
            val icrf = ceres.at<ICRF>(time)

            icrf.position[0] shouldBe (-2.2887358680299896 plusOrMinus 1e-6)
            icrf.position[1] shouldBe (0.7613682864183494 plusOrMinus 1e-6)
            icrf.position[2] shouldBe (0.8249889493185274 plusOrMinus 1e-6)
            icrf.velocity[0] shouldBe (-0.004522105973639024 plusOrMinus 1e-6)
            icrf.velocity[1] shouldBe (-0.009582289706142557 plusOrMinus 1e-6)
            icrf.velocity[2] shouldBe (-0.003598833220781664 plusOrMinus 1e-6)
        }
        "asteroid: mean anomaly" {
            val time = TDB(2458886.5)

            val ceres = Asteroid(
                semiMajorAxis = 2.768873850275102.au,
                eccentricity = 7.705857791518426E-02,
                inclination = 2.718528770987308E+01.deg,
                argumentOfPerihelion = 1.328964361683606E+02.deg, // W
                longitudeOfAscendingNode = 2.336112629072238E+01.deg, // OM
                meanAnomaly = 1.382501360489816E+02.deg,
                epoch = time,
                rotation = null,
            )

            val (r) = ceres.at<ICRF>(time)

            val sun = Vector3D(-0.004105894975783999, 0.006739680703224941, 0.002956344702049446)
            val horizons = Vector3D(1.334875927366032E+00, -2.239607658161781E+00, -1.328895183461897E+00)

            val s = r + sun - horizons
            val epsilon = 0.001.m

            s[0] shouldBeLessThan epsilon.value
            s[1] shouldBeLessThan epsilon.value
            s[2] shouldBeLessThan epsilon.value
        }
    }
}
