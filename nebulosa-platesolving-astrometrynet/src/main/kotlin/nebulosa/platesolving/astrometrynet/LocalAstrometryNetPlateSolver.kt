package nebulosa.platesolving.astrometrynet

import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import java.io.File
import java.time.Duration

class LocalAstrometryNetPlateSolver(private val path: String = "") : PlateSolver {

    override fun solve(
        file: File,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): Calibration {
        TODO("Not yet implemented")
    }
}
