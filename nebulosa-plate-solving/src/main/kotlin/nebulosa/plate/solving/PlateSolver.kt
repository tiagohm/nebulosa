package nebulosa.plate.solving

import nebulosa.math.Angle
import java.nio.file.Path
import java.time.Duration

interface PlateSolver {

    fun solve(
        path: Path,
        blind: Boolean = true,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0,
        radius: Angle = 0.0,
        downsampleFactor: Int = 2,
        timeout: Duration? = null,
    ): PlateSolution
}
