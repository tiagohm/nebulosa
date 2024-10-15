package nebulosa.commandline

import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.ExecuteResultHandler
import org.apache.commons.exec.ExecuteStreamHandler
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

class CommandLineListenerHandler : ExecuteResultHandler, ExecuteStreamHandler {

    private val listeners = ConcurrentHashMap.newKeySet<CommandLineListener>(1)

    @Volatile private var errorStream: BufferedReader? = null
    @Volatile private var inputStream: BufferedWriter? = null
    @Volatile private var outputStream: BufferedReader? = null
    @Volatile private var errorThread: Thread? = null
    @Volatile private var outputThread: Thread? = null

    fun registerCommandLineListener(listener: CommandLineListener) {
        listeners.add(listener)
    }

    fun unregisterCommandLineListener(listener: CommandLineListener) {
        listeners.remove(listener)
    }

    override fun setProcessErrorStream(stream: InputStream) {
        errorStream = stream.bufferedReader()
    }

    override fun setProcessInputStream(stream: OutputStream) {
        inputStream = stream.bufferedWriter()
    }

    override fun setProcessOutputStream(stream: InputStream) {
        outputStream = stream.bufferedReader()
    }

    override fun start() {
        errorThread?.interrupt()
        errorThread = LineReaderThread(errorStream!!, listeners)
        errorThread!!.start()

        outputThread?.interrupt()
        outputThread = LineReaderThread(outputStream!!, listeners)
        outputThread!!.start()

        listeners.forEach { it.onStarted() }
    }

    override fun stop() {
        errorThread?.interrupt()
        errorThread = null

        outputThread?.interrupt()
        outputThread = null
    }

    override fun onProcessFailed(e: ExecuteException) {
        listeners.forEach { it.onExited(-1, e) }
    }

    override fun onProcessComplete(exitValue: Int) {
        listeners.forEach { it.onExited(exitValue, null) }
    }

    @Synchronized
    fun write(text: String) {
        with(inputStream ?: return) {
            write(text)
            newLine()
            flush()
        }
    }
}
