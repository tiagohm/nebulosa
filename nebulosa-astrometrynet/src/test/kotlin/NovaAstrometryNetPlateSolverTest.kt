import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.astrometrynet.platesolver.NovaAstrometryNetPlateSolver
import nebulosa.math.deg
import nebulosa.math.toArcsec
import nebulosa.math.toDegrees
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class NovaAstrometryNetPlateSolverTest {

    @Test
    fun solve() {
        val calibration = SOLVER.solve(FILE, null, centerRA = 290.0.deg, centerDEC = 11.0.deg, radius = 2.0.deg)

        calibration.orientation.toDegrees shouldBeExactly 90.0397051079753
        calibration.scale.toArcsec shouldBeExactly 2.0675124414774606
        calibration.radius.toDegrees shouldBeExactly 0.36561535148882157
        calibration.rightAscension.toDegrees shouldBeExactly 290.237669307
        calibration.declination.toDegrees shouldBeExactly 11.1397773954
    }

    @Test
    fun blindSolve() {
        val calibration = SOLVER.solve(FILE, null)

        calibration.orientation.toDegrees shouldBeExactly 90.0397051079753
        calibration.scale.toArcsec shouldBeExactly 2.0675124414774606
        calibration.radius.toDegrees shouldBeExactly 0.36561535148882157
        calibration.rightAscension.toDegrees shouldBeExactly 290.237669307
        calibration.declination.toDegrees shouldBeExactly 11.1397773954
    }

    companion object {

        @JvmStatic private val FILE = dataDirectory.concat("ldn673s_block1123.jpg")
        @JvmStatic private val SERVICE = NovaAstrometryNetService()
        @JvmStatic private val SOLVER = NovaAstrometryNetPlateSolver(SERVICE)
    }
}
