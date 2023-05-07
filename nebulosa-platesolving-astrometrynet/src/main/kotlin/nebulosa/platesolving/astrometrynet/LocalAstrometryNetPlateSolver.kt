package nebulosa.platesolving.astrometrynet

import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Angle.Companion.deg
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.deleteRecursively

/**
 * @see <a href="http://astrometry.net/doc/readme.html">README</a>
 */
class LocalAstrometryNetPlateSolver(private val path: String) : PlateSolver {

    override fun solve(
        file: File,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): Calibration {
        val args = arrayListOf<String>()

        args.add(path)

        // args.add("-v")
        // args.add("--timestamp")

        val baseName = UUID.randomUUID().toString()
        args.add("--out")
        args.add(baseName)

        args.add("--overwrite")

        val outFolder = Files.createTempDirectory("localplatesolver")
        args.add("--dir")
        args.add("$outFolder")

        args.add("--cpulimit")
        args.add("${timeout?.toSeconds() ?: 300}")

        args.add("--scale-units")
        args.add("degwidth")

        args.add("--guess-scale")

        args.add("--crpix-center")

        args.add("--downsample")
        args.add("$downsampleFactor")

        args.add("--no-verify")
        args.add("--no-plots")
        // args.add("--resort")

        if (!blind) {
            args.add("--ra")
            args.add("${centerRA.degrees}")

            args.add("--dec")
            args.add("${centerDEC.degrees}")

            args.add("--radius")
            args.add("${radius.degrees}")
        }

        args.add("$file")

        LOG.info("local solving. command={}", args)

        val process = ProcessBuilder(args)
            // .inheritIO()
            .start()

        val buffer = process.inputReader()

        var calibration = Calibration.EMPTY

        val parseThread = thread {
            for (line in buffer.lines()) {
                LOG.info(line)

                calibration = calibration
                    .parseFieldCenter(line)
                    .parseFieldRotation(line)
                    .parsePixelScale(line)
                    .parseFieldSize(line)
            }

            // Populate WCS headers from calibration info.
            // TODO: calibration = calibration.copy()

            LOG.info("astrometry.net solved. calibration={}", calibration)
        }

        try {
            process.waitFor()
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
            return FIELD_CENTER_REGEX.matchEntire(line)
                ?.let { copy(rightAscension = it.groupValues[1].toDouble().deg, declination = it.groupValues[2].toDouble().deg) }
                ?: this
        }

        @JvmStatic
        private fun Calibration.parseFieldSize(line: String): Calibration {
            return FIELD_SIZE_REGEX.matchEntire(line)
                ?.let {
                    val width = it.groupValues[1].toDouble().arcmin
                    val height = it.groupValues[2].toDouble().arcmin
                    copy(width = width, height = height)
                } ?: this
        }

        @JvmStatic
        private fun Calibration.parseFieldRotation(line: String): Calibration {
            return FIELD_ROTATION_REGEX.matchEntire(line)
                ?.let { copy(orientation = it.groupValues[1].toDouble().deg) }
                ?: this
        }

        @JvmStatic
        private fun Calibration.parsePixelScale(line: String): Calibration {
            return PIXEL_SCALE_REGEX.matchEntire(line)
                ?.let { copy(scale = it.groupValues[1].toDouble().arcsec) }
                ?: this
        }
    }
}
