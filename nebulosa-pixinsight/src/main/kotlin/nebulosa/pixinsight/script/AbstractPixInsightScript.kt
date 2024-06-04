package nebulosa.pixinsight.script

import nebulosa.common.exec.CommandLine
import nebulosa.common.exec.LineReadListener
import java.util.concurrent.CompletableFuture

abstract class AbstractPixInsightScript<T> : PixInsightScript<T>, LineReadListener, CompletableFuture<T>() {

    override fun onInputRead(line: String) = Unit

    override fun onErrorRead(line: String) = Unit

    protected open fun beforeRun() = Unit

    protected abstract fun processOnComplete(exitCode: Int): T?

    final override fun run(runner: PixInsightScriptRunner) = runner.run(this)

    final override fun handleCommandLine(commandLine: CommandLine) {
        commandLine.whenComplete { exitCode, exception ->
            try {
                if (isDone) return@whenComplete
                else if (exception != null) completeExceptionally(exception)
                else complete(processOnComplete(exitCode))
            } finally {
                commandLine.unregisterLineReadListener(this)
            }
        }

        commandLine.registerLineReadListener(this)
        beforeRun()
        commandLine.start()
    }
}
