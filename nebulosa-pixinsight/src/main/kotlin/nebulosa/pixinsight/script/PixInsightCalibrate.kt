package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightCalibrate(
    override val slot: Int,
    private val workingDirectory: Path,
    private val targetPath: Path,
    private val darkPath: Path? = null,
    private val flatPath: Path? = null,
    private val biasPath: Path? = null,
    private val compress: Boolean = false,
    private val use32Bit: Boolean = false,
) : AbstractPixInsightScript<PixInsightCalibrate.Output>() {

    private data class Input(
        @JvmField val targetPath: Path,
        @JvmField val outputDirectory: Path,
        @JvmField val statusPath: Path,
        @JvmField val masterDark: Path? = null,
        @JvmField val masterFlat: Path? = null,
        @JvmField val masterBias: Path? = null,
        @JvmField val compress: Boolean = false,
        @JvmField val use32Bit: Boolean = false,
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
        resource("pixinsight/Calibrate.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(targetPath, workingDirectory, statusPath, darkPath, flatPath, biasPath, compress, use32Bit)
    override val arguments = listOf("-x=${execute(scriptPath, input)}")

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
