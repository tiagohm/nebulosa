package nebulosa.siril.command

import nebulosa.commandline.CommandLineListener
import nebulosa.commandline.CommandLineListenerHandler
import nebulosa.log.di
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import java.nio.file.Path

class SirilCommandLine(executablePath: Path) : Runnable, CancellationListener, AutoCloseable {

    private val handler = CommandLineListenerHandler()

    private val executor = DefaultExecutor.builder()
        .setExecuteStreamHandler(handler)
        .get()

    private val commandLine = CommandLine.parse("$executablePath")
        .addArgument("-s").addArgument("-")

    @Volatile private var started = false

    init {
        executor.watchdog = ExecuteWatchdog.builder()
            .setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT_DURATION)
            .get()
    }

    val isRunning
        get() = started && executor.watchdog.isWatching

    fun registerCommandLineListener(listener: CommandLineListener) {
        handler.registerCommandLineListener(listener)
    }

    fun unregisterCommandLineListener(listener: CommandLineListener) {
        handler.unregisterCommandLineListener(listener)
    }

    override fun run() {
        if (!isRunning) {
            started = true
            executor.execute(commandLine, handler)
            execute(Requires)
        }
    }

    internal fun write(command: String) {
        if (isRunning) {
            LOG.di(command)
            handler.write(command)
        }
    }

    fun <T> execute(command: SirilCommand<T>): T {
        return command.write(this)
    }

    override fun onCancel(source: CancellationSource) {
        close()
    }

    override fun close() {
        execute(Exit)

        if (isRunning) {
            executor.watchdog.destroyProcess()
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SirilCommandLine>()
    }
}
