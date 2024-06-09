package nebulosa.astrometrynet.platesolver

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.image.Image
import nebulosa.math.Angle
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import java.nio.file.Path
import java.time.Duration

data class LibAstrometryNetPlateSolver(private val solver: LibAstrometryNet) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
        cancellationToken: CancellationToken,
    ): PlateSolution {
        return PlateSolution.NO_SOLUTION
    }
}
