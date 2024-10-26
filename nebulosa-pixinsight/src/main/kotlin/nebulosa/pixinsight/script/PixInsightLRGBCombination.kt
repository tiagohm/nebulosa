package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

@Suppress("ArrayInDataClass")
data class PixInsightLRGBCombination(
    override val slot: Int,
    private val outputPath: Path,
    private val luminancePath: Path? = null,
    private val redPath: Path? = null,
    private val greenPath: Path? = null,
    private val bluePath: Path? = null,
    private val weights: DoubleArray = DEFAULT_CHANNEL_WEIGHTS,
) : AbstractPixInsightScript<PixInsightLRGBCombination.Output>() {

    @Suppress("ArrayInDataClass")
    private data class Input(
        @JvmField val outputPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val luminancePath: Path?,
        @JvmField val redPath: Path?,
        @JvmField val greenPath: Path?,
        @JvmField val bluePath: Path?,
        @JvmField val channelWeights: DoubleArray,
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
        require(weights.size >= 4) { "invalid weights size: ${weights.size}" }
        resource("pixinsight/LRGBCombination.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(outputPath, statusPath, luminancePath, redPath, greenPath, bluePath, weights)
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

    companion object {

        private val DEFAULT_CHANNEL_WEIGHTS = doubleArrayOf(1.0, 1.0, 1.0, 1.0)
    }
}
