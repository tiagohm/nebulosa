package nebulosa.astrometrynet.platesolving

import nebulosa.common.process.ProcessExecutor
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.deleteRecursively

/**
 * @see <a href="http://astrometry.net/doc/readme.html">README</a>
 */
class LocalAstrometryNetPlateSolver(path: Path) : PlateSolver {

    private val executor = ProcessExecutor(path)

    override fun solve(
        path: Path,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): Calibration {
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

        if (!blind) {
            arguments["--ra"] = centerRA.toDegrees
            arguments["--dec"] = centerDEC.toDegrees
            arguments["--radius"] = radius.toDegrees
        }

        arguments["$path"] = null

        val process = executor.execute(arguments, Duration.ZERO, path.parent)

        val buffer = process.inputReader()

        var calibration = Calibration(false, 0.0, 0.0, 0.0, 0.0)

        val parseThread = thread {
            for (line in buffer.lines()) {
                calibration = calibration
                    .parseFieldCenter(line)
                    .parseFieldRotation(line)
                    .parsePixelScale(line)
                    .parseFieldSize(line)
            }

            // Populate WCS headers from calibration info.
            // TODO: calibration = calibration.copy()
            // TODO: Mark calibration as solved.

            LOG.info("astrometry.net solved. calibration={}", calibration)
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

        return calibration
    }

    companion object {

        private const val NUMBER_REGEX = "([\\d.+-]+)"

        @JvmStatic private val LOG = loggerFor<LocalAstrometryNetPlateSolver>()
        @JvmStatic private val FIELD_CENTER_REGEX = Regex(".*Field center: \\(RA,Dec\\) = \\($NUMBER_REGEX, $NUMBER_REGEX\\).*")
        @JvmStatic private val FIELD_SIZE_REGEX = Regex(".*Field size: $NUMBER_REGEX x $NUMBER_REGEX arcminutes.*")
        @JvmStatic private val FIELD_ROTATION_REGEX = Regex(".*Field rotation angle: up is $NUMBER_REGEX degrees.*")
        @JvmStatic private val PIXEL_SCALE_REGEX = Regex(".*pixel scale $NUMBER_REGEX arcsec/pix.*")

        @JvmStatic
        private fun Calibration.parseFieldCenter(line: String): Calibration {
            return nebulosa.astrometrynet.platesolving.LocalAstrometryNetPlateSolver.FIELD_CENTER_REGEX.matchEntire(line)
                ?.let { copy(rightAscension = it.groupValues[1].toDouble().deg, declination = it.groupValues[2].toDouble().deg) }
                ?: this
        }

        @JvmStatic
        private fun Calibration.parseFieldSize(line: String): Calibration {
            return nebulosa.astrometrynet.platesolving.LocalAstrometryNetPlateSolver.FIELD_SIZE_REGEX.matchEntire(line)
                ?.let {
                    val width = it.groupValues[1].toDouble().arcmin
                    val height = it.groupValues[2].toDouble().arcmin
                    copy(width = width, height = height)
                } ?: this
        }

        @JvmStatic
        private fun Calibration.parseFieldRotation(line: String): Calibration {
            return nebulosa.astrometrynet.platesolving.LocalAstrometryNetPlateSolver.FIELD_ROTATION_REGEX.matchEntire(line)
                ?.let { copy(orientation = it.groupValues[1].toDouble().deg) }
                ?: this
        }

        @JvmStatic
        private fun Calibration.parsePixelScale(line: String): Calibration {
            return nebulosa.astrometrynet.platesolving.LocalAstrometryNetPlateSolver.PIXEL_SCALE_REGEX.matchEntire(line)
                ?.let { copy(scale = it.groupValues[1].toDouble().arcsec) }
                ?: this
        }
    }
}
