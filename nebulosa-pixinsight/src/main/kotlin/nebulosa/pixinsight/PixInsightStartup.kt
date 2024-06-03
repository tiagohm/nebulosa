package nebulosa.pixinsight

import nebulosa.common.exec.CommandLine
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightStartup(private val slot: Int = SLOT) : PixInsightScript<Boolean> {

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val outputPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Startup.js")!!.transferAndClose(scriptPath.outputStream())

        scriptPath.toFile().deleteOnExit()
        outputPath.toFile().deleteOnExit()
    }

    override val arguments = listOf("-r=\"$scriptPath,$outputPath\"", if (slot > 0) "-n=$slot" else "-n")

    override fun beforeRun(commandLine: CommandLine, result: CompletableFuture<Boolean>) {
        thread(isDaemon = true, name = "Startup Path Watcher") {
            var count = 0

            try {
                while (count++ < 60) {
                    Thread.sleep(500)

                    if (outputPath.readText() == "STARTED") {
                        result.complete(true)
                        return@thread
                    }
                }

                result.complete(false)
            } catch (e: Throwable) {
                result.completeExceptionally(e)
            }
        }
    }

    override fun processOnComplete(pid: Long, exitCode: Int): Boolean {
        return exitCode == 0
    }

    companion object {

        const val SLOT = 256
    }
}
