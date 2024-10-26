package nebulosa.commandline

import nebulosa.log.d
import nebulosa.log.loggerFor
import java.io.BufferedReader

internal data class LineReaderThread(
    private val reader: BufferedReader,
    private val listeners: Iterable<CommandLineListener>,
) : Thread("Command Line Reader") {

    init {
        isDaemon = true
    }

    override fun run() {
        while (true) {
            val line = reader.readLine() ?: break
            LOG.d(line)
            listeners.forEach { it.onLineRead(line) }
        }
    }

    companion object {

        private val LOG = loggerFor<LineReaderThread>()
    }
}
