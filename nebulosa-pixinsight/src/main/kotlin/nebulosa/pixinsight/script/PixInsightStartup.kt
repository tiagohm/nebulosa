package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import kotlin.concurrent.timer
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightStartup(private val slot: Int) : AbstractPixInsightScript<PixInsightStartup.Output>() {

    data class Output(
        override val success: Boolean,
        override val errorMessage: String? = null,
    ) : PixInsightScript.Output {

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

    override val arguments = listOf("-r=${execute(0, scriptPath, outputPath)}", if (slot > 0) "-n=$slot" else "-n")

    override fun beforeRun() {
        var count = 0

        timer("PixInsight Startup Timer", true, 1000L, 500L) {
            if (outputPath.readText() == "STARTED") {
                complete(Output.SUCCESS)
                cancel()
            } else if (count >= 60) {
                complete(Output.FAILED)
                cancel()
            }

            count++
        }
    }

    override fun processOnComplete(exitCode: Int) = Output.FAILED

    override fun close() {
        scriptPath.deleteIfExists()
        outputPath.deleteIfExists()
    }
}
