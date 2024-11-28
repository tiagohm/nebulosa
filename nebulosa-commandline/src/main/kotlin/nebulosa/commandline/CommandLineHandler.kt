package nebulosa.commandline

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class CommandLineHandler(
    private val hasError: Boolean = true,
    private val hasInput: Boolean = true,
) {

    private val listeners = ConcurrentHashMap.newKeySet<CommandLineListener>(1)
    private val running = AtomicBoolean()

    @Volatile private var errorStream: BufferedReader? = null
    @Volatile private var inputStream: BufferedWriter? = null
    @Volatile private var outputStream: BufferedReader? = null
    @Volatile private var errorThread: Thread? = null
    @Volatile private var outputThread: Thread? = null
    @Volatile private var process: Process? = null

    val isRunning
        get() = running.get()

    val pid
        get() = process?.pid() ?: 0L

    fun registerCommandLineListener(listener: CommandLineListener) {
        listeners.add(listener)
    }

    fun unregisterCommandLineListener(listener: CommandLineListener) {
        listeners.remove(listener)
    }

    internal fun setProcessErrorStream(stream: InputStream) {
        errorStream = stream.bufferedReader()
    }

    internal fun setProcessInputStream(stream: OutputStream) {
        inputStream = stream.bufferedWriter()
    }

    internal fun setProcessOutputStream(stream: InputStream) {
        outputStream = stream.bufferedReader()
    }

    @Synchronized
    internal fun start(process: Process) {
        if (!running.get()) {
            if (hasError) {
                errorThread?.interrupt()
                errorThread = LineReaderThread(errorStream!!, listeners)
                errorThread!!.start()
            }

            if (hasInput) {
                outputThread?.interrupt()
                outputThread = LineReaderThread(outputStream!!, listeners)
                outputThread!!.start()
            }

            val pid = process.pid()
            listeners.forEach { it.onStarted(pid) }
            this.process = process

            running.set(true)
        }
    }

    internal fun stop() {
        errorThread?.interrupt()
        errorThread?.join()
        errorThread = null

        outputThread?.interrupt()
        outputThread?.join()
        outputThread = null

        running.set(false)
        process = null
    }

    internal fun onProcessFailed(e: Throwable) {
        listeners.forEach { it.onExited(-1, e) }
    }

    internal fun onProcessComplete(exitValue: Int) {
        listeners.forEach { it.onExited(exitValue, null) }
    }

    fun write(text: String) {
        while (!running.get()) {
            Thread.sleep(100)
        }

        val stream = inputStream ?: return

        stream.write(text)
        stream.newLine()
        stream.flush()
    }

    fun kill() {
        val process = process ?: return

        if (process.isAlive) {
            process.destroyForcibly()
            process.waitFor()
        }
    }
}
