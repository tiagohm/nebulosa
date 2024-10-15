import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.astap.platesolver.AstapPlateSolver
import nebulosa.math.*
import nebulosa.test.NonGitHubOnly
import nebulosa.test.fits.ASTROMETRY_GALACTIC_CENTER_FITS
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@NonGitHubOnly
class AstapPlateSolverTest {

    @Test
    fun blind() {
        val solver = AstapPlateSolver(Path("astap"))
        val solution = solver.solve(ASTROMETRY_GALACTIC_CENTER_FITS, null)

        solution.solved.shouldBeTrue()
        solution.orientation.toDegrees shouldBe (-179.8412 plusOrMinus 0.1)
        solution.scale.toArcsec shouldBe (2.3204 plusOrMinus 0.01)
        solution.rightAscension.toDegrees shouldBe ("17 45 47.4".hours.toDegrees plusOrMinus (1.0 / 3600.0))
        solution.declination.toDegrees shouldBe ("-029 07 26.3".deg.toDegrees plusOrMinus (1.0 / 3600.0))
        solution.width.toArcmin shouldBe (49.50 plusOrMinus 0.01)
        solution.height.toArcmin shouldBe (39.60 plusOrMinus 0.01)
    }

    @Test
    fun nearest() {
        val solver = AstapPlateSolver(Path("astap"))
        val solution = solver.solve(ASTROMETRY_GALACTIC_CENTER_FITS, null, "17 47 14.4".hours, "-029 01 06.9".deg, 4.deg)

        solution.solved.shouldBeTrue()
        solution.orientation.toDegrees shouldBe (-179.8412 plusOrMinus 0.1)
        solution.scale.toArcsec shouldBe (2.3204 plusOrMinus 0.01)
        solution.rightAscension.toDegrees shouldBe ("17 45 47.4".hours.toDegrees plusOrMinus (1.0 / 3600.0))
        solution.declination.toDegrees shouldBe ("-029 07 26.3".deg.toDegrees plusOrMinus (1.0 / 3600.0))
        solution.width.toArcmin shouldBe (49.50 plusOrMinus 0.01)
        solution.height.toArcmin shouldBe (39.60 plusOrMinus 0.01)
    }
}
