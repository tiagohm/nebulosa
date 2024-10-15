package nebulosa.siril.platesolver

import nebulosa.image.Image
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.platesolver.PlateSolver
import nebulosa.siril.command.PlateSolve
import nebulosa.siril.command.SirilCommandLine
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
        downsampleFactor: Int, timeout: Duration,
    ) = SirilCommandLine(executablePath).use {
        it.run()
        val useCenterCoordinates = radius > 0.0 && radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()
        it.execute(PlateSolve(path!!, focalLength, pixelSize, useCenterCoordinates, centerRA, centerDEC, downsampleFactor, timeout))
    }
}
