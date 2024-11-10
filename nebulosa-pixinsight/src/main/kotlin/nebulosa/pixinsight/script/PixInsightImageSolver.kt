package nebulosa.pixinsight.script

import com.sun.jna.Platform
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.math.max

data class PixInsightImageSolver(
    override val slot: Int,
    private val targetPath: Path,
    private val centerRA: Angle,
    private val centerDEC: Angle,
    private val pixelSize: Double = 0.0, // µm
    private val resolution: Double = 0.0, // arcsec/px
    private val focalLength: Double = 0.0, // mm
    private val timeout: Duration = Duration.ZERO,
) : AbstractPixInsightScript<PixInsightImageSolver.Output>() {

    private data class Input(
        @JvmField val targetPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val centerRA: Double, // deg
        @JvmField val centerDEC: Double, // deg
        @JvmField val pixelSize: Double = 0.0,
        @JvmField val resolution: Double = 0.0,
        @JvmField val focalLength: Double = 0.0,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val rightAscension: Angle = 0.0,
        @JvmField val declination: Angle = 0.0,
        @JvmField val resolution: Angle = 0.0,
        @JvmField val pixelSize: Double = 0.0,
        @JvmField val focalLength: Double = 0.0,
        @JvmField val width: Angle = 0.0,
        @JvmField val height: Angle = 0.0,
        // @JvmField val rotation: Angle = 0.0,
        @JvmField val imageWidth: Double = 0.0,
        @JvmField val imageHeight: Double = 0.0,
        @JvmField val astrometricSolutionSummary: String = "",
    ) : PixInsightScriptOutput {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")
    private val includePaths = ArrayList<Path>(INCLUDE_PATHS.size)

    init {
        val scriptsDir = if (Platform.isWindows()) WINDOWS_SCRIPTS_DIR else LINUX_SCRIPTS_DIR

        for (includePath in INCLUDE_PATHS) {
            val inputPath = Path.of(scriptsDir, "AdP", includePath)
            val outputPath = Path.of("${scriptPath.parent}", includePath)
            inputPath.inputStream().transferAndClose(outputPath.outputStream())
            includePaths.add(outputPath)
        }

        resource("pixinsight/ImageSolver.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(targetPath, statusPath, centerRA.toDegrees, centerDEC.toDegrees, pixelSize, resolution, focalLength)
    override val arguments = listOf("-x=${execute(scriptPath, input)}")

    override fun processOnExit(exitCode: Int, output: CompletableFuture<Output>) {
        if (exitCode == 0) {
            val count = max(60, timeout.toSeconds()).toInt()

            repeat(count) {
                if (output.isDone) return
                Thread.sleep(1000)
                val status = statusPath.parseStatus<Output>() ?: return@repeat
                output.complete(status)
            }
        }

        output.complete(Output.FAILED)
    }

    override fun close() {
        scriptPath.deleteIfExists()
        statusPath.deleteIfExists()
        includePaths.forEach { it.deleteIfExists() }
    }

    companion object {

        private const val WINDOWS_SCRIPTS_DIR = "C:\\Program Files\\PixInsight\\src\\scripts"
        private const val LINUX_SCRIPTS_DIR = "/opt/PixInsight/src/scripts"

        private val INCLUDE_PATHS = listOf("Projections.js", "WCSmetadata.jsh", "AstronomicalCatalogs.jsh")
    }
}
