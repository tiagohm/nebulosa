package nebulosa.siril.platesolver

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.image.Image
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import nebulosa.siril.command.PlateSolve
import nebulosa.siril.command.SirilCommandLine
import java.nio.file.Path
import java.time.Duration

data class SirilPlateSolver(private val executablePath: Path) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?, cancellationToken: CancellationToken
    ): PlateSolution {
        val commandLine = SirilCommandLine(executablePath)

        return try {
            commandLine.run()
            cancellationToken.listen(commandLine)
            val useCenterCoordinates = radius > 0.0 && radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()
            commandLine.execute(PlateSolve(path!!, 0.0, useCenterCoordinates, centerRA, centerDEC, downsampleFactor, timeout ?: Duration.ZERO))
        } finally {
            cancellationToken.unlisten(commandLine)
            commandLine.close()
        }
    }
}
