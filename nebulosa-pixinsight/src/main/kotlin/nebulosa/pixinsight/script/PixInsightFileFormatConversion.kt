package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightFileFormatConversion(
    override val slot: Int,
    private val inputPath: Path,
    private val outputPath: Path,
) : AbstractPixInsightScript<PixInsightFileFormatConversion.Output>() {

    private data class Input(
        @JvmField val inputPath: Path,
        @JvmField val outputPath: Path,
        @JvmField val statusPath: Path,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
    ) : PixInsightScriptOutput {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/FileFormatConversion.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-x=${execute(scriptPath, Input(inputPath, outputPath, statusPath))}")

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
