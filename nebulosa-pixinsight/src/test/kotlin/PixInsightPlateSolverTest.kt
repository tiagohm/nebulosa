import io.kotest.core.annotation.EnabledIf
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.*
import nebulosa.pixinsight.platesolver.PixInsightPlateSolver
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.math.roundToInt

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightPlateSolverTest : AbstractFitsAndXisfTest() {

    init {
        "solver" {
            val runner = PixInsightScriptRunner(Path.of("PixInsight"))
            val pixelSize = 6.58
            val resolution = 6.78.arcsec
            val solver = PixInsightPlateSolver(runner, pixelSize, resolution = resolution)
            val path = download("https://nova.astrometry.net/image/14603", "jpg")
            val centerRA = "06 40 51".hours
            val centerDEC = "09 49 53".deg

            val solution = solver.solve(path, null, centerRA, centerDEC)

            solution.scale.toArcsec shouldBe (6.774 plusOrMinus 1e-3)
            solution.rightAscension.formatHMS() shouldBe "06h40m51.8s"
            solution.declination.formatSignedDMS() shouldBe "+009°49'53.6\""
            solution.width.toArcmin shouldBe (90.321 plusOrMinus 1e-3)
            solution.height.toArcmin shouldBe (59.386 plusOrMinus 1e-3)
            solution.widthInPixels.roundToInt() shouldBeExactly 800
            solution.heightInPixels.roundToInt() shouldBeExactly 526
        }
    }
}
