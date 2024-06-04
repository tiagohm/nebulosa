package nebulosa.pixinsight.script

import nebulosa.common.exec.CommandLine
import java.io.Closeable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

interface PixInsightScript<T> : Future<T>, Closeable {

    val arguments: Iterable<String>

    fun handleCommandLine(commandLine: CommandLine)

    fun run(runner: PixInsightScriptRunner)

    fun runSync(runner: PixInsightScriptRunner): T {
        run(runner)
        return get()
    }

    fun runSync(runner: PixInsightScriptRunner, timeout: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T {
        run(runner)
        return get(timeout, unit)
    }

    companion object {

        const val DEFAULT_SLOT = 256
        const val UNSPECIFIED_SLOT = 0
    }
}
