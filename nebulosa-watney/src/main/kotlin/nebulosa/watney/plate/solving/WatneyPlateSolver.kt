package nebulosa.watney.plate.solving

import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import java.time.Duration

class WatneyPlateSolver : PlateSolver<Image> {

    override fun solve(
        input: Image, blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        TODO("Not yet implemented")
    }
}
