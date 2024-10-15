package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.stardetector.StarPoint
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightDetectStars(
    override val slot: Int,
    private val targetPath: Path,
    private val minSNR: Double = 0.0,
    private val invert: Boolean = false,
    private val timeout: Duration = Duration.ZERO,
) : AbstractPixInsightScript<PixInsightDetectStars.Output>() {

    private data class Input(
        @JvmField val targetPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val minSNR: Double = 0.0,
        @JvmField val invert: Boolean = false,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val stars: List<Star> = emptyList(),
    ) : PixInsightScriptOutput {

        override fun toString() = "Output(success=$success, errorMessage=$errorMessage, stars=${stars.size})"

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    data class Star(
        override val x: Double = 0.0,
        override val y: Double = 0.0,
        override val flux: Double = 0.0,
        @JvmField val size: Double = 0.0,
        @JvmField val bkg: Double = 0.0,
        @JvmField val x0: Int = 0,
        @JvmField val y0: Int = 0,
        @JvmField val x1: Int = 0,
        @JvmField val y1: Int = 0,
        @JvmField val nmax: Int = 0,
        override val snr: Double = 0.0,
        @JvmField val peak: Double = 0.0,
        override val hfd: Double = 0.0,
    ) : StarPoint

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/DetectStars.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-x=${execute(scriptPath, Input(targetPath, statusPath, minSNR, invert))}")

    override fun processOnExit(exitCode: Int, output: CompletableFuture<Output>) {
        if (exitCode == 0) {
            repeat(60) {
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
    }
}
