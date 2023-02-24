package nebulosa.platesolving.astrometrynet

import nebulosa.math.Angle
import nebulosa.platesolving.PlateSolver
import nebulosa.platesolving.PlateSolvingCalibration
import java.io.File
import java.time.Duration

class LocalAstrometryNetPlateSolver(path: String = "") : PlateSolver {

    override fun solve(
        file: File,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): PlateSolvingCalibration {
        TODO("Not yet implemented")
    }
}
