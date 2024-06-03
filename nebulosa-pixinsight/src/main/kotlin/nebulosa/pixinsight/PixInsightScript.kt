package nebulosa.pixinsight

import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.LineReadListener
import java.util.concurrent.CompletableFuture

interface PixInsightScript<T> : LineReadListener {

    val arguments: List<String>

    override fun onInputRead(line: String) = Unit

    override fun onErrorRead(line: String) = Unit

    fun beforeRun(commandLine: CommandLine, result: CompletableFuture<T>) = Unit

    fun processOnComplete(pid: Long, exitCode: Int): T

    fun run(runner: PixInsightScriptRunner): CompletableFuture<T> {
        return runner.run(this)
    }

    fun runSync(runner: PixInsightScriptRunner): T {
        return run(runner).get()
    }
}
