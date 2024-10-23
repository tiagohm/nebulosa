package nebulosa.astrometrynet.platesolver

import nebulosa.commandline.CommandLine
import nebulosa.commandline.CommandLineHandler
import nebulosa.fits.FitsHeader
import nebulosa.image.Image
import nebulosa.io.seekableSource
import nebulosa.log.di
import nebulosa.log.e
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.readBytes

/**
 * @see <a href="http://astrometry.net/doc/readme.html">README</a>
 */
data class LocalAstrometryNetPlateSolver(private val executablePath: Path) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration,
    ): PlateSolution {
        requireNotNull(path) { "path is required" }

        val outFolder = Files.createTempDirectory("lanps-")
        val outName = UUID.randomUUID().toString()

        val commands = mutableListOf(
            "$executablePath",
            "--out", outName,
            "--overwrite",
            "--dir", "$outFolder",
            "--cpulimit", timeout.takeIf { it.toSeconds() > 0 }?.toSeconds()?.toString() ?: "300",
            "--scale-units", "degwidth",
            "--guess-scale",
            "--crpix-center",
            "--downsample", "$downsampleFactor",
            "--no-verify",
            "--no-plots",
            "--skip-solved",
            "--no-remove-lines",
            "--uniformize", "0",
            // "--resort"
        )

        if (radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()) {
            commands.add("--ra")
            commands.add("${centerRA.toDegrees}")
            commands.add("--dec")
            commands.add("${centerDEC.toDegrees}")
            commands.add("--radius")
            commands.add("${radius.toDegrees}")
        }

        commands.add("$path")

        val commandLine = CommandLine(commands)

        try {
            val handler = CommandLineHandler()
            val result = commandLine.execute(handler)

            if (result.isSuccess) {
                LOG.di("astrometry.net exited. code={}", result.exitCode)
                val solved = Path.of("$outFolder", "$outName.solved").takeIf { it.exists() }?.readBytes()?.takeIf { it.size == 1 }?.get(0)?.toInt() == 1

                if (solved) {
                    val wcsPath = Path.of("$outFolder", "$outName.wcs")
                    val header = wcsPath.seekableSource().use { FitsHeader.from(it) }
                    return PlateSolution.from(header)!!
                }
            } else {
                LOG.e("astrometry.net failed. code={}", result.exitCode, result.exception)
            }

            return PlateSolution.NO_SOLUTION
        } finally {
            outFolder.deleteRecursively()
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<LocalAstrometryNetPlateSolver>()
    }
}
