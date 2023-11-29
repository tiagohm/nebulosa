import io.kotest.core.annotation.EnabledIf
import nebulosa.fits.Fits
import nebulosa.imaging.Image
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.test.FitsStringSpec
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.watney.plate.solving.WatneyPlateSolver
import nebulosa.watney.plate.solving.quad.CompactQuadDatabase
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class WatnetPlateSolverTest : FitsStringSpec() {

    init {
        val image = Image.open(Fits("/home/tiagohm/Imagens/NGC3372-LRGB_ASTAP_MONO.fit").also(Fits::read))
        val quadDir = Path.of("/home/tiagohm/Downloads/watneyqdb")
        val quadDatabase = CompactQuadDatabase(quadDir)
        val solver = WatneyPlateSolver(quadDatabase)

        "nearby" {
            val centerRA = "10 45 08.5".hours
            val centerDEC = "−59 52 04".deg
            println(solver.solve(image, centerRA, centerDEC, 0.deg))
        }
    }
}
