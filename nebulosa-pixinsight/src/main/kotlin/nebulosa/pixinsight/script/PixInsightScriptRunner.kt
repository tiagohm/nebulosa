package nebulosa.pixinsight.script

import nebulosa.commandline.CommandLine
import nebulosa.commandline.CommandLineHandler
import nebulosa.commandline.CommandLineListener
import nebulosa.log.d
import nebulosa.log.de
import nebulosa.log.di
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture

data class PixInsightScriptRunner(private val executablePath: Path) {

    fun <T : PixInsightScriptOutput> run(script: PixInsightScript<T>): CompletableFuture<T> {
        val commands = mutableListOf("$executablePath")
        script.arguments.forEach(commands::add)
        DEFAULT_ARGS.forEach(commands::add)

        LOG.d("running {} script", script.name)

        val handler = CommandLineHandler()
        val commandLine = CommandLine(commands)
        val completable = CompletableFuture<T>()

        completable.whenComplete { o, e ->
            if (e is CancellationException) handler.kill()
            if (o != null) LOG.di("{} completed. output={}", script.name, o)
            else LOG.de("{} completed with exception", script.name, e)
        }

        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onStarted(pid: Long) {
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

        commandLine.execute(handler)

        return completable
    }

    companion object {

        private val DEFAULT_ARGS = arrayOf("--automation-mode", "--no-startup-scripts")
        private val LOG = loggerFor<PixInsightScriptRunner>()
    }
}
