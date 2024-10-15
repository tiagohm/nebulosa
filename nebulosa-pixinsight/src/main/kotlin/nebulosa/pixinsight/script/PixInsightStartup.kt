package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.pixinsight.script.PixInsightPixelMath.Output
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.timer
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightStartup(override val slot: Int) : AbstractPixInsightScript<PixInsightStartup.Output>() {

    data class Output(
        override val success: Boolean,
        override val errorMessage: String? = null,
    ) : PixInsightScriptOutput {

        companion object {

            @JvmStatic val SUCCESS = Output(true)
            @JvmStatic val FAILED = Output(false)
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val outputPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Startup.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-r=${execute(scriptPath, outputPath, 0)}", if (slot > 0) "-n=$slot" else "-n")

    override fun processOnStart(output: CompletableFuture<Output>) {
        var count = 0

        timer("PixInsight Startup Timer", true, 1000L, 500L) {
            if (outputPath.readText() == "STARTED") {
                output.complete(Output.SUCCESS)
                cancel()
            } else if (count >= 120) {
                output.complete(Output.FAILED)
                cancel()
            }

            count++
        }
    }

    override fun close() {
        scriptPath.deleteIfExists()
        outputPath.deleteIfExists()
    }
}
