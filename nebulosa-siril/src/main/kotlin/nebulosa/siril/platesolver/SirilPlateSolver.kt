package nebulosa.siril.platesolver

import nebulosa.image.Image
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import nebulosa.siril.command.PlateSolve
import nebulosa.siril.command.SirilCommandLine
import nebulosa.util.concurrency.cancellation.CancellationToken
import java.nio.file.Path
import java.time.Duration

data class SirilPlateSolver(
    private val executablePath: Path,
    private val focalLength: Double = 0.0,
    private val pixelSize: Double = 0.0,
) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration, cancellationToken: CancellationToken
    ): PlateSolution {
        val commandLine = SirilCommandLine(executablePath)

        return try {
            commandLine.run()
            cancellationToken.listen(commandLine)
            val useCenterCoordinates = radius > 0.0 && radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()
            commandLine.execute(PlateSolve(path!!, focalLength, pixelSize, useCenterCoordinates, centerRA, centerDEC, downsampleFactor, timeout))
        } finally {
            cancellationToken.unlisten(commandLine)
            commandLine.close()
        }
    }
}
