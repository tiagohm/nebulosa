package nebulosa.platesolver

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.image.Image
import nebulosa.math.Angle
import java.nio.file.Path
import java.time.Duration

interface PlateSolver {

    fun solve(
        path: Path?, image: Image?,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
        downsampleFactor: Int = 0, timeout: Duration = Duration.ZERO,
        cancellationToken: CancellationToken = CancellationToken.NONE,
    ): PlateSolution
}
