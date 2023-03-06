import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.io.resource
import nebulosa.math.Angle.Companion.deg
import nebulosa.platesolving.astrometrynet.NovaAstrometryNetPlateSolver
import java.io.File

class NovaAstrometryNetPlateSolverTest : StringSpec() {

    init {
        val file = File.createTempFile("nova", ".jpg").also { resource("ldn673s_block1123.jpg")!!.transferTo(it.outputStream()) }

        "!solve" {
            val service = NovaAstrometryNetService()
            val solver = NovaAstrometryNetPlateSolver(service)

            val calibration = solver.solve(file, blind = false, centerRA = 290.0.deg, centerDEC = 11.0.deg, radius = 2.0.deg)

            calibration.orientation.degrees shouldBeExactly 90.0397051079753
            calibration.scale shouldBeExactly 2.0675124414774606
            calibration.radius.degrees shouldBeExactly 0.36561535148882157
            calibration.rightAscension.degrees shouldBeExactly 290.237669307
            calibration.declination.degrees shouldBeExactly 11.1397773954
        }
        "!blind solve" {
            val service = NovaAstrometryNetService()
            val solver = NovaAstrometryNetPlateSolver(service)

            val calibration = solver.solve(file, blind = true)

            calibration.orientation.degrees shouldBeExactly 90.0397051079753
            calibration.scale shouldBeExactly 2.0675124414774606
            calibration.radius.degrees shouldBeExactly 0.36561535148882157
            calibration.rightAscension.degrees shouldBeExactly 290.237669307
            calibration.declination.degrees shouldBeExactly 11.1397773954
        }
    }
}
