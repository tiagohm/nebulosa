import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.astrometrynet.platesolver.LocalAstrometryNetPlateSolver
import nebulosa.math.*
import nebulosa.platesolver.Parity
import nebulosa.test.NonGitHubOnly
import nebulosa.test.download
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

// git clone --depth=1 https://github.com/dstndstn/astrometry.net.git
// cd astrometry.net
// sudo apt install libcairo2-dev libnetpbm10-dev netpbm libpng-dev libjpeg-dev zlib1g-dev libbz2-dev libcfitsio-dev wcslib-dev
// make
// make install
// Download https://github.com/dstndstn/astrometry.net/blob/main/demo/index-4119.fits and save into INSTALL_DIR/data

@NonGitHubOnly
class LocalAstrometryNetPlateSolverTest {

    @Test
    fun solve() {
        val solution = SOLVER.solve(IMAGE, null, "06 01 10.6".hours, "+004 40 12.5".deg, 60.0.deg)

        solution.solved.shouldBeTrue()
        solution.rightAscension.formatHMS() shouldBe "06h01m10.6s"
        solution.declination.formatSignedDMS() shouldBe "+004°40'12.5\""
        solution.scale.toArcsec shouldBe (286.37 plusOrMinus 1e-2)
        solution.orientation.toDegrees shouldBe (-165.65 plusOrMinus 1e-2)
        solution.parity shouldBe Parity.NORMAL
        solution.width.toDegrees shouldBe (71.6075 plusOrMinus 1e-4)
        solution.height.toDegrees shouldBe (53.6951 plusOrMinus 1e-4)
        solution.widthInPixels shouldBeExactly 900.0
        solution.heightInPixels shouldBeExactly 675.0

        solution.contains("BP_2_0").shouldBeTrue()
    }

    @Test
    fun blindSolve() {
        val solution = SOLVER.solve(IMAGE, null)

        solution.solved.shouldBeTrue()
        solution.rightAscension.formatHMS() shouldBe "06h01m10.6s"
        solution.declination.formatSignedDMS() shouldBe "+004°40'12.5\""
        solution.scale.toArcsec shouldBe (286.37 plusOrMinus 1e-2)
        solution.orientation.toDegrees shouldBe (-165.65 plusOrMinus 1e-2)
        solution.parity shouldBe Parity.NORMAL
        solution.width.toDegrees shouldBe (71.6075 plusOrMinus 1e-4)
        solution.height.toDegrees shouldBe (53.6951 plusOrMinus 1e-4)
        solution.widthInPixels shouldBeExactly 900.0
        solution.heightInPixels shouldBeExactly 675.0

        solution.contains("BP_2_0").shouldBeTrue()
    }

    companion object {

        private val IMAGE by lazy { download("https://github.com/dstndstn/astrometry.net/blob/main/demo/apod5.jpg?raw=true") }
        private val SOLVER = LocalAstrometryNetPlateSolver(Path("/usr/local/astrometry/bin/solve-field"))
    }
}
