package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightAlign(
    override val slot: Int,
    private val workingDirectory: Path,
    private val referencePath: Path,
    private val targetPath: Path,
) : AbstractPixInsightScript<PixInsightAlign.Output>() {

    private data class Input(
        @JvmField val referencePath: Path,
        @JvmField val targetPath: Path,
        @JvmField val outputDirectory: Path,
        @JvmField val statusPath: Path,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
        @JvmField val outputMaskImage: Path? = null,
        @JvmField val totalPairMatches: Int = 0,
        @JvmField val inliers: Int = 0,
        @JvmField val overlapping: Int = 0,
        @JvmField val regularity: Double = 0.0,
        @JvmField val quality: Double = 0.0,
        @JvmField val rmsError: Double = 0.0,
        @JvmField val rmsErrorDev: Double = 0.0,
        @JvmField val peakErrorX: Double = 0.0,
        @JvmField val peakErrorY: Double = 0.0,
        @JvmField val h11: Double = 0.0,
        @JvmField val h12: Double = 0.0,
        @JvmField val h13: Double = 0.0,
        @JvmField val h21: Double = 0.0,
        @JvmField val h22: Double = 0.0,
        @JvmField val h23: Double = 0.0,
        @JvmField val h31: Double = 0.0,
        @JvmField val h32: Double = 0.0,
        @JvmField val h33: Double = 0.0,
    ) : PixInsightScriptOutput {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Align.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-x=${execute(scriptPath, Input(referencePath, targetPath, workingDirectory, statusPath))}")

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
