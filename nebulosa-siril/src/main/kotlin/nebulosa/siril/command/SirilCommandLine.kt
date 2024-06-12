package nebulosa.siril.command

import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.exec.CommandLineListener
import nebulosa.common.exec.commandLine
import java.io.Closeable
import java.nio.file.Path

data class SirilCommandLine(private val executablePath: Path) : Runnable, CancellationListener, Closeable {

    private val commandLine = commandLine {
        executablePath(executablePath)
        putArg("-s", "-")
    }

    val isRunning
        get() = commandLine.isRunning

    val pid
        get() = commandLine.pid

    fun registerCommandLineListener(listener: CommandLineListener) {
        commandLine.registerCommandLineListener(listener)
    }

    fun unregisterCommandLineListener(listener: CommandLineListener) {
        commandLine.unregisterCommandLineListener(listener)
    }

    override fun run() {
        if (!commandLine.isRunning) {
            commandLine.start()
            execute(Requires)
        }
    }

    internal fun write(command: String) {
        if (commandLine.isRunning) {
            commandLine.writer.println(command)
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
        commandLine.stop()
    }
}
