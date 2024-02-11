package nebulosa.astrometrynet.plate.solving

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import java.nio.file.Path
import java.time.Duration

data class LibAstrometryNetPlateSolver(private val solver: LibAstrometryNet) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
        cancellationToken: CancellationToken?,
    ): PlateSolution {
        return PlateSolution.NO_SOLUTION
    }
}
