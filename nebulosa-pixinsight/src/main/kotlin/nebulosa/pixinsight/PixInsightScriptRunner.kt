package nebulosa.pixinsight

import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.commandLine
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

data class PixInsightScriptRunner(private val executablePath: Path) {

    private val runningScripts = ConcurrentHashMap<PixInsightScript<*>, CommandLine>()

    @Synchronized
    fun <T> run(script: PixInsightScript<T>): CompletableFuture<T> {
        require(!runningScripts.containsKey(script)) { "script is already running" }

        val result = CompletableFuture<T>()

        val commandLine = commandLine {
            executablePath(executablePath)
            script.arguments.forEach(::putArg)
            DEFAULT_ARGS.forEach(::putArg)
        }

        commandLine.whenComplete { exitCode, exception ->
            try {
                if (result.isDone) return@whenComplete
                else if (exception != null) result.completeExceptionally(exception)
                else result.complete(script.processOnComplete(commandLine.pid, exitCode))
            } finally {
                commandLine.unregisterLineReadListener(script)
                runningScripts.remove(script)
            }
        }

        commandLine.registerLineReadListener(script)
        script.beforeRun(commandLine, result)

        if (!commandLine.isDone) {
            runningScripts[script] = commandLine
            commandLine.start()
        } else {
            result.complete(script.processOnComplete(commandLine.pid, commandLine.exitCode))
        }

        return result
    }

    fun stop(script: PixInsightScript<*>) {
        runningScripts[script]?.stop()
    }

    companion object {

        @JvmStatic private val DEFAULT_ARGS = arrayOf("--automation-mode", "--no-startup-scripts")
    }
}
