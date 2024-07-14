package nebulosa.astrometrynet.platesolver

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.exec.CommandLineListener
import nebulosa.common.exec.commandLine
import nebulosa.image.Image
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.function.Supplier
import kotlin.io.path.deleteRecursively

/**
 * @see <a href="http://astrometry.net/doc/readme.html">README</a>
 */
data class LocalAstrometryNetPlateSolver(private val executablePath: Path) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration,
        cancellationToken: CancellationToken,
    ): PlateSolution {
        requireNotNull(path) { "path is required" }

        val outFolder = Files.createTempDirectory("localplatesolver")

        val cmd = commandLine {
            executablePath(executablePath)
            workingDirectory(path.parent)

            putArg("--out", UUID.randomUUID().toString())
            putArg("--overwrite")

            putArg("--dir", outFolder)

            putArg("--cpulimit", timeout.takeIf { it.toSeconds() > 0 }?.toSeconds() ?: 300)
            putArg("--scale-units", "degwidth")
            putArg("--guess-scale")
            putArg("--crpix-center")
            putArg("--downsample", downsampleFactor)
            putArg("--no-verify")
            putArg("--no-plots")
            // putArg("--resort")

            if (radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()) {
                putArg("--ra", centerRA.toDegrees)
                putArg("--dec", centerDEC.toDegrees)
                putArg("--radius", radius.toDegrees)
            }

            putArg("$path")
        }

        val solution = PlateSolutionLineReader()

        try {
            cancellationToken.listen(cmd)
            cmd.registerCommandLineListener(solution)
            cmd.start()
            LOG.info("astrometry.net exited. code={}", cmd.get())
            return solution.get()
        } catch (e: Throwable) {
            LOG.error("astronomy.net failed.", e)
            return PlateSolution.NO_SOLUTION
        } finally {
            cancellationToken.unlisten(cmd)
            outFolder.deleteRecursively()
        }
    }

    private class PlateSolutionLineReader : CommandLineListener.OnLineRead, Supplier<PlateSolution> {

        @Volatile private var fieldCenter: DoubleArray? = null
        @Volatile private var fieldRotation: Angle = 0.0
        @Volatile private var pixelScale: Angle = 0.0
        @Volatile private var fieldSize: DoubleArray? = null

        override fun onLineRead(line: String) {
            fieldCenter(line)?.also { fieldCenter = it }
                ?: fieldRotation(line)?.also { fieldRotation = it }
                ?: pixelScale(line)?.also { pixelScale = it }
                ?: fieldSize(line)?.also { fieldSize = it }
        }

        override fun get(): PlateSolution {
            val (rightAscension, declination) = fieldCenter!!
            val (width, height) = fieldSize!!

            return PlateSolution(true, fieldRotation, pixelScale, rightAscension, declination, width, height)
        }

        companion object {

            private const val NUMBER_REGEX = "([\\d.+-]+)"

            @JvmStatic private val FIELD_CENTER_REGEX = Regex("Field center: \\(RA,Dec\\) = \\($NUMBER_REGEX, $NUMBER_REGEX\\)")
            @JvmStatic private val FIELD_SIZE_REGEX = Regex("Field size: $NUMBER_REGEX x $NUMBER_REGEX arcminutes")
            @JvmStatic private val FIELD_ROTATION_REGEX = Regex("Field rotation angle: up is $NUMBER_REGEX degrees")
            @JvmStatic private val PIXEL_SCALE_REGEX = Regex("pixel scale $NUMBER_REGEX arcsec/pix")

            @JvmStatic
            private fun fieldCenter(line: String): DoubleArray? {
                return FIELD_CENTER_REGEX.find(line)
                    ?.let { doubleArrayOf(it.groupValues[1].toDouble().deg, it.groupValues[2].toDouble().deg) }
            }

            @JvmStatic
            private fun fieldSize(line: String): DoubleArray? {
                return FIELD_SIZE_REGEX.find(line)
                    ?.let { doubleArrayOf(it.groupValues[1].toDouble().arcmin, it.groupValues[2].toDouble().arcmin) }
            }

            @JvmStatic
            private fun fieldRotation(line: String): Angle? {
                return FIELD_ROTATION_REGEX.find(line)
                    ?.let { it.groupValues[1].toDouble().deg }
            }

            @JvmStatic
            private fun pixelScale(line: String): Angle? {
                return PIXEL_SCALE_REGEX.find(line)
                    ?.let { it.groupValues[1].toDouble().arcsec }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<LocalAstrometryNetPlateSolver>()
    }
}
