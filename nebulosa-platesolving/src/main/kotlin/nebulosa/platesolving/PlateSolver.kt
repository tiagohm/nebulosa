package nebulosa.platesolving

import nebulosa.math.Angle
import java.nio.file.Path
import java.time.Duration

interface PlateSolver {

    fun solve(
        path: Path,
        blind: Boolean = true,
        centerRA: Angle = Angle.ZERO, centerDEC: Angle = Angle.ZERO,
        radius: Angle = Angle.ZERO,
        downsampleFactor: Int = 2,
        timeout: Duration? = null,
    ): Calibration
}
