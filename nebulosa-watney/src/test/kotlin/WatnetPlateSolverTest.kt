import nebulosa.imaging.Image
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.test.FitsStringSpec
import nebulosa.watney.plate.solving.WatneyPlateSolver

class WatnetPlateSolverTest : FitsStringSpec() {

    init {
        val image = Image.open(M6707HH)
        val solver = WatneyPlateSolver()

        "nearby" {
            val centerRA = "08 51 20.100".hours
            val centerDEC = "+11 48 43.00".deg
            solver.solve(image, false, centerRA, centerDEC, 1.deg)
        }
    }
}
