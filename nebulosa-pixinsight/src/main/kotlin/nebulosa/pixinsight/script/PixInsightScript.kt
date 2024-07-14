package nebulosa.pixinsight.script

import nebulosa.common.exec.CommandLine
import java.io.Closeable
import java.util.concurrent.Future

sealed interface PixInsightScript<T : PixInsightScript.Output> : Future<T>, Closeable {

    sealed interface Output {

        val success: Boolean

        val errorMessage: String?
    }

    val arguments: Iterable<String>

    fun startCommandLine(commandLine: CommandLine)

    fun run(runner: PixInsightScriptRunner)

    fun runSync(runner: PixInsightScriptRunner): T {
        run(runner)
        return get()
    }

    companion object {

        const val DEFAULT_SLOT = 256
        const val UNSPECIFIED_SLOT = 0
    }
}
