package nebulosa.astrometrynet.plate.solving

import nebulosa.common.process.ProcessExecutor
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.deleteRecursively

/**
 * @see <a href="http://astrometry.net/doc/readme.html">README</a>
 */
class LocalAstrometryNetPlateSolver(path: Path) : PlateSolver<Path> {

    private val executor = ProcessExecutor(path)

    override fun solve(
        input: Path,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        val arguments = mutableMapOf<String, Any?>()

        arguments["--out"] = UUID.randomUUID().toString()
        arguments["--overwrite"] = null

        val outFolder = Files.createTempDirectory("localplatesolver")
        arguments["--dir"] = outFolder

        arguments["--cpulimit"] = timeout?.toSeconds() ?: 300
        arguments["--scale-units"] = "degwidth"
        arguments["--guess-scale"] = null
        arguments["--crpix-center"] = null
        arguments["--downsample"] = downsampleFactor
        arguments["--no-verify"] = null
        arguments["--no-plots"] = null
        // args["--resort"] = null

        if (radius.toDegrees >= 0.1) {
            arguments["--ra"] = centerRA.toDegrees
            arguments["--dec"] = centerDEC.toDegrees
            arguments["--radius"] = radius.toDegrees
        }

        arguments["$input"] = null

        val process = executor.execute(arguments, Duration.ZERO, input.parent)

        val buffer = process.inputReader()

        var solution = PlateSolution(false, 0.0, 0.0, 0.0, 0.0)

        val parseThread = thread {
            for (line in buffer.lines()) {
                solution = solution
                    .parseFieldCenter(line)
                    .parseFieldRotation(line)
                    .parsePixelScale(line)
                    .parseFieldSize(line)
            }

            // Populate WCS headers from calibration info.
            // TODO: calibration = calibration.copy()
            // TODO: Mark calibration as solved.

            LOG.info("astrometry.net solved. calibration={}", solution)
        }

        try {
            process.waitFor()
            LOG.info("astrometry.net exited. code={}", process.exitValue())
        } catch (e: InterruptedException) {
            parseThread.interrupt()
            process.destroyForcibly()
        } finally {
            outFolder.deleteRecursively()
        }

        return solution
    }

    companion object {

        private const val NUMBER_REGEX = "([\\d.+-]+)"

        @JvmStatic private val LOG = loggerFor<LocalAstrometryNetPlateSolver>()
        @JvmStatic private val FIELD_CENTER_REGEX = Regex(".*Field center: \\(RA,Dec\\) = \\($NUMBER_REGEX, $NUMBER_REGEX\\).*")
        @JvmStatic private val FIELD_SIZE_REGEX = Regex(".*Field size: $NUMBER_REGEX x $NUMBER_REGEX arcminutes.*")
        @JvmStatic private val FIELD_ROTATION_REGEX = Regex(".*Field rotation angle: up is $NUMBER_REGEX degrees.*")
        @JvmStatic private val PIXEL_SCALE_REGEX = Regex(".*pixel scale $NUMBER_REGEX arcsec/pix.*")

        @JvmStatic
        private fun PlateSolution.parseFieldCenter(line: String): PlateSolution {
            return FIELD_CENTER_REGEX.matchEntire(line)
                ?.let { copy(rightAscension = it.groupValues[1].toDouble().deg, declination = it.groupValues[2].toDouble().deg) }
                ?: this
        }

        @JvmStatic
        private fun PlateSolution.parseFieldSize(line: String): PlateSolution {
            return FIELD_SIZE_REGEX.matchEntire(line)
                ?.let {
                    val width = it.groupValues[1].toDouble().arcmin
                    val height = it.groupValues[2].toDouble().arcmin
                    copy(width = width, height = height)
                } ?: this
        }

        @JvmStatic
        private fun PlateSolution.parseFieldRotation(line: String): PlateSolution {
            return FIELD_ROTATION_REGEX.matchEntire(line)
                ?.let { copy(orientation = it.groupValues[1].toDouble().deg) }
                ?: this
        }

        @JvmStatic
        private fun PlateSolution.parsePixelScale(line: String): PlateSolution {
            return PIXEL_SCALE_REGEX.matchEntire(line)
                ?.let { copy(scale = it.groupValues[1].toDouble().arcsec) }
                ?: this
        }
    }
}
