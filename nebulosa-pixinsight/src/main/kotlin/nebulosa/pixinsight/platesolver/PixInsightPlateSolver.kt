package nebulosa.pixinsight.platesolver

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.image.Image
import nebulosa.math.Angle
import nebulosa.math.toArcsec
import nebulosa.pixinsight.script.PixInsightImageSolver
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import java.nio.file.Path
import java.time.Duration

data class PixInsightPlateSolver(
    private val runner: PixInsightScriptRunner,
    private val pixelSize: Double, // µm
    private val resolution: Angle = 0.0,
    private val focalDistance: Double = 0.0, // mm
    private val slot: Int = PixInsightScript.UNSPECIFIED_SLOT,
) : PlateSolver {

    init {
        require(resolution > 0.0 || focalDistance > 0.0) { "resolution or focalDistance are required" }
    }

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration,
        cancellationToken: CancellationToken
    ): PlateSolution {
        require(path != null) { "path must be provided" }
        val solver = PixInsightImageSolver(slot, path, centerRA, centerDEC, pixelSize, resolution.toArcsec, focalDistance)
            .use { it.runSync(runner) }

        if (solver.success) {
            val m = ROTATION_REGEX.find(solver.astrometricSolutionSummary, 0)
            val rotation = m?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            return PlateSolution(
                true, rotation, solver.resolution,
                solver.rightAscension, solver.declination,
                solver.width, solver.height, widthInPixels = solver.imageWidth,
                heightInPixels = solver.imageHeight,
            )
        }

        return PlateSolution.NO_SOLUTION
    }

    companion object {

        @JvmStatic val ROTATION_REGEX = Regex("Rotation\\s*\\.+\\s*([\\d.]+)")
    }
}
