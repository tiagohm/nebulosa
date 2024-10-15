package nebulosa.astrometrynet.platesolver

import nebulosa.commandline.CommandLineListener
import nebulosa.commandline.CommandLineListenerHandler
import nebulosa.image.Image
import nebulosa.log.di
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
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
    ): PlateSolution {
        requireNotNull(path) { "path is required" }

        val outFolder = Files.createTempDirectory("localplatesolver")

        val commandLine = CommandLine.parse("$executablePath")
            .addArgument("--out").addArgument(UUID.randomUUID().toString())
            .addArgument("--overwrite")
            .addArgument("--dir").addArgument("$outFolder")
            .addArgument("--cpulimit").addArgument(timeout.takeIf { it.toSeconds() > 0 }?.toSeconds()?.toString() ?: "300")
            .addArgument("--scale-units").addArgument("degwidth")
            .addArgument("--guess-scale")
            .addArgument("--crpix-center")
            .addArgument("--downsample").addArgument("$downsampleFactor")
            .addArgument("--no-verify")
            .addArgument("--no-plots")
        // .addArgument("--resort")

        if (radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()) {
            commandLine.addArgument("--ra").addArgument("${centerRA.toDegrees}")
                .addArgument("--dec").addArgument("${centerDEC.toDegrees}")
                .addArgument("--radius").addArgument("${radius.toDegrees}")
        }

        val solution = PlateSolutionLineReader()
        val handler = CommandLineListenerHandler()

        val executor = DefaultExecutor.builder()
            .setWorkingDirectory(path.parent.toFile())
            .setExecuteStreamHandler(handler)
            .get()

        executor.watchdog = ExecuteWatchdog.builder()
            .setTimeout(timeout.takeIf { it.toSeconds() > 0 } ?: Duration.ofMinutes(5))
            .get()

        commandLine.addArgument("$path")

        return try {
            handler.registerCommandLineListener(solution)
            LOG.di("astrometry.net exited. code={}", executor.execute(commandLine, handler))
            solution.get()
        } finally {
            outFolder.deleteRecursively()
        }
    }

    private class PlateSolutionLineReader : CommandLineListener, Supplier<PlateSolution> {

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
