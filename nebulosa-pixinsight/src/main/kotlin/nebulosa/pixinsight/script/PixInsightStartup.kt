package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import kotlin.concurrent.timer
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightStartup(private val slot: Int) : AbstractPixInsightScript<Boolean>() {

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val outputPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Startup.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-r=${parameterize(0, scriptPath, outputPath)}", if (slot > 0) "-n=$slot" else "-n")

    override fun beforeRun() {
        var count = 0

        timer("PixInsight Startup Timer", true, 1000L, 500L) {
            if (outputPath.readText() == "STARTED") {
                complete(true)
                cancel()
            } else if (count >= 60) {
                complete(false)
                cancel()
            }

            count++
        }
    }

    override fun processOnComplete(exitCode: Int) = false

    override fun close() {
        scriptPath.deleteIfExists()
        outputPath.deleteIfExists()
    }
}
