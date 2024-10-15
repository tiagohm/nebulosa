package nebulosa.pixinsight.script

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

sealed interface PixInsightScript<T : PixInsightScriptOutput> : AutoCloseable {

    val slot: Int

    val name: String

    val arguments: Iterable<String>

    fun processOnStart(output: CompletableFuture<T>) = Unit

    fun processLine(line: String, output: CompletableFuture<T>) = Unit

    fun processOnExit(exitCode: Int, output: CompletableFuture<T>) = Unit

    fun run(runner: PixInsightScriptRunner) = runner.run(this)

    fun runSync(runner: PixInsightScriptRunner): T = run(runner).get()

    fun runSync(runner: PixInsightScriptRunner, timeout: Long, unit: TimeUnit): T = run(runner).get(timeout, unit)

    companion object {

        const val DEFAULT_SLOT = 256
        const val UNSPECIFIED_SLOT = 0
    }
}
