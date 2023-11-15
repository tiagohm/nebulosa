import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.math.deg
import nebulosa.math.toArcsec
import nebulosa.math.toDegrees
import nebulosa.astrometrynet.platesolving.NovaAstrometryNetPlateSolver
import java.nio.file.Files
import kotlin.io.path.outputStream

@Ignored
class NovaAstrometryNetPlateSolverTest : StringSpec() {

    init {
        val file = Files.createTempFile("nova", ".jpg")
            .also { resource("ldn673s_block1123.jpg")!!.transferAndClose(it.outputStream()) }

        "solve" {
            val service = NovaAstrometryNetService()
            val solver = NovaAstrometryNetPlateSolver(service)

            val calibration = solver.solve(file, blind = false, centerRA = 290.0.deg, centerDEC = 11.0.deg, radius = 2.0.deg)

            calibration.orientation.toDegrees shouldBeExactly 90.0397051079753
            calibration.scale.toArcsec shouldBeExactly 2.0675124414774606
            calibration.radius.toDegrees shouldBeExactly 0.36561535148882157
            calibration.rightAscension.toDegrees shouldBeExactly 290.237669307
            calibration.declination.toDegrees shouldBeExactly 11.1397773954
        }
        "blind solve" {
            val service = NovaAstrometryNetService()
            val solver = NovaAstrometryNetPlateSolver(service)

            val calibration = solver.solve(file, blind = true)

            calibration.orientation.toDegrees shouldBeExactly 90.0397051079753
            calibration.scale.toArcsec shouldBeExactly 2.0675124414774606
            calibration.radius.toDegrees shouldBeExactly 0.36561535148882157
            calibration.rightAscension.toDegrees shouldBeExactly 290.237669307
            calibration.declination.toDegrees shouldBeExactly 11.1397773954
        }
    }
}
