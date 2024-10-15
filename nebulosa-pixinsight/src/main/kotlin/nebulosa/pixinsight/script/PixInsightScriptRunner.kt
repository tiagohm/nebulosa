package nebulosa.pixinsight.script

import nebulosa.commandline.CommandLineListener
import nebulosa.commandline.CommandLineListenerHandler
import nebulosa.log.d
import nebulosa.log.de
import nebulosa.log.di
import nebulosa.log.loggerFor
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture

data class PixInsightScriptRunner(private val executablePath: Path) {

    fun <T : PixInsightScriptOutput> run(script: PixInsightScript<T>): CompletableFuture<T> {
        val commandLine = CommandLine.parse("$executablePath")
        script.arguments.forEach(commandLine::addArgument)
        DEFAULT_ARGS.forEach(commandLine::addArgument)

        LOG.d("running {} script: {}", script.name, commandLine)

        val handler = CommandLineListenerHandler()
        val completable = CompletableFuture<T>()

        val executor = DefaultExecutor.builder()
            .setExecuteStreamHandler(handler)
            .get()

        executor.watchdog = ExecuteWatchdog.builder()
            .setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT_DURATION)
            .get()

        completable.whenComplete { o, e ->
            if (e is CancellationException) executor.watchdog.destroyProcess()
            if (o != null) LOG.di("{} completed. output={}", script.name, o)
            else LOG.de("{} completed with exception", script.name, e)
        }

        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onStarted() {
                script.processOnStart(completable)
            }

            override fun onLineRead(line: String) {
                script.processLine(line, completable)
            }

            override fun onExited(exitCode: Int, exception: Throwable?) {
                LOG.d("{} script finished. done={}, exitCode={}", script.name, completable.isDone, exitCode, exception)

                if (completable.isDone) return
                if (exception != null) completable.completeExceptionally(exception)
                else script.processOnExit(exitCode, completable)
            }
        })

        executor.execute(commandLine, handler)

        return completable
    }

    companion object {

        @JvmStatic private val DEFAULT_ARGS = arrayOf("--automation-mode", "--no-startup-scripts")
        @JvmStatic private val LOG = loggerFor<PixInsightScriptRunner>()
    }
}
