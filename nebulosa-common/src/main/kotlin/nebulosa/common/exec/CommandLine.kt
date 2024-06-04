package nebulosa.common.exec

import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

inline fun commandLine(action: CommandLine.Builder.() -> Unit): CommandLine {
    return CommandLine.Builder().also(action).get()
}

data class CommandLine internal constructor(
    private val builder: ProcessBuilder,
    private val listeners: HashSet<LineReadListener>,
) : CompletableFuture<Int>(), CancellationListener {

    @Volatile private var process: Process? = null
    @Volatile private var waiter: ProcessWaiter? = null
    @Volatile private var inputReader: StreamLineReader? = null
    @Volatile private var errorReader: StreamLineReader? = null

    val command: List<String>
        get() = builder.command()

    val pid
        get() = process?.pid() ?: -1L

    val exitCode
        get() = process?.takeIf { !it.isAlive }?.exitValue() ?: -1

    val writer = PrintStream(object : OutputStream() {

        override fun write(b: Int) {
            process?.outputStream?.write(b)
        }

        override fun write(b: ByteArray) {
            process?.outputStream?.write(b)
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            process?.outputStream?.write(b, off, len)
        }

        override fun flush() {
            process?.outputStream?.flush()
        }

        override fun close() {
            process?.outputStream?.close()
        }
    }, true)

    fun registerLineReadListener(listener: LineReadListener) {
        listeners.add(listener)
    }

    fun unregisterLineReadListener(listener: LineReadListener) {
        listeners.remove(listener)
    }

    @Synchronized
    fun start(timeout: Duration = Duration.ZERO): CommandLine {
        if (process == null) {
            process = try {
                builder.start()
            } catch (e: Throwable) {
                completeExceptionally(e)
                return this
            }

            if (listeners.isNotEmpty()) {
                inputReader = StreamLineReader(process!!.inputStream, false)
                inputReader!!.start()

                errorReader = StreamLineReader(process!!.errorStream, true)
                errorReader!!.start()
            }

            waiter = ProcessWaiter(process!!, timeout.toMillis())
            waiter!!.start()
        }

        return this
    }

    @Synchronized
    fun stop() {
        waiter?.interrupt()
        waiter = null

        inputReader?.interrupt()
        inputReader = null

        errorReader?.interrupt()
        errorReader = null

        process?.destroyForcibly()
        process?.waitFor()
        process = null
    }

    fun get(timeout: Duration): Int {
        return get(timeout.toNanos(), TimeUnit.NANOSECONDS)
    }

    override fun onCancel(source: CancellationSource) {
        stop()
    }

    private inner class ProcessWaiter(
        private val process: Process,
        private val timeout: Long,
    ) : Thread("Command Line Process Waiter") {

        init {
            isDaemon = true
        }

        override fun run() {
            try {
                if (timeout > 0L) {
                    process.waitFor(timeout, TimeUnit.MILLISECONDS)
                } else {
                    process.waitFor()
                }

                inputReader?.waitFor()
                errorReader?.waitFor()
            } catch (ignored: InterruptedException) {
            } finally {
                if (process.isAlive) {
                    process.destroyForcibly()
                    process.waitFor()
                }

                complete(process.exitValue())
            }
        }
    }

    private inner class StreamLineReader(
        stream: InputStream,
        private val isError: Boolean,
    ) : Thread("Command Line ${if (isError) "Error" else "Input"} Stream Line Reader") {

        private val reader = stream.bufferedReader()
        private val completable = CompletableFuture<Unit>()

        init {
            isDaemon = true
        }

        override fun run() {
            try {
                while (true) {
                    val line = reader.readLine() ?: break
                    if (isError) listeners.forEach { it.onErrorRead(line) }
                    else listeners.forEach { it.onInputRead(line) }
                }
            } catch (ignored: Throwable) {
            } finally {
                completable.complete(Unit)
                reader.close()
            }
        }

        fun waitFor() {
            return completable.join()
        }
    }

    class Builder : Supplier<CommandLine> {

        private val builder = ProcessBuilder()
        private val environment by lazy { builder.environment() }
        private val arguments = mutableMapOf<String, Any?>()
        private var executable = ""
        private val listeners = HashSet<LineReadListener>(1)

        fun executablePath(path: Path) = executable("$path")

        fun executable(executable: String) = run { this.executable = executable }

        fun env(key: String) = environment[key]

        fun putEnv(key: String, value: String) = environment.put(key, value)

        fun removeEnv(key: String) = environment.remove(key)

        fun hasEnv(key: String) = key in environment

        fun arg(name: String) = arguments[name]

        fun putArg(name: String, value: Any) = arguments.put(name, value)

        fun putArg(name: String) = arguments.put(name, null)

        fun removeArg(name: String) = arguments.remove(name)

        fun hasArg(name: String) = name in arguments

        fun workingDirectory(path: Path): Unit = run { builder.directory(path.toFile()) }

        fun registerLineReadListener(listener: LineReadListener) = listeners.add(listener)

        fun unregisterLineReadListener(listener: LineReadListener) = listeners.remove(listener)

        override fun get(): CommandLine {
            val args = ArrayList<String>(1 + arguments.size * 2)

            require(executable.isNotBlank()) { "executable must not be blank" }

            args.add(executable)

            for ((key, value) in arguments) {
                args.add(key)
                value?.toString()?.also(args::add)
            }

            builder.command(args)

            return CommandLine(builder, listeners)
        }
    }
}
