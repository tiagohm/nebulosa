package nebulosa.siril.command

import nebulosa.commandline.CommandLine
import nebulosa.commandline.CommandLineHandler
import nebulosa.commandline.CommandLineListener
import nebulosa.log.di
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

class SirilCommandLine(executablePath: Path) : Runnable, CancellationListener, AutoCloseable {

    private val handler = CommandLineHandler()
    private val commandLine = CommandLine(listOf("$executablePath", "-s", "-"))
    private val started = AtomicBoolean()

    val isRunning
        get() = started.get()

    fun registerCommandLineListener(listener: CommandLineListener) {
        handler.registerCommandLineListener(listener)
    }

    fun unregisterCommandLineListener(listener: CommandLineListener) {
        handler.unregisterCommandLineListener(listener)
    }

    override fun run() {
        if (started.compareAndSet(false, true)) {
            CompletableFuture.runAsync { commandLine.execute(handler) }
            execute(Requires)
        }
    }

    internal fun write(command: String) {
        if (started.get()) {
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
        handler.kill()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SirilCommandLine>()
    }
}
