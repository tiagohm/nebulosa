package nebulosa.indi.protocol.parser

import nebulosa.log.loggerFor
import java.io.Closeable

class INDIProtocolReader(
    private val parser: INDIProtocolParser,
    priority: Int = NORM_PRIORITY,
) : Thread(), Closeable {

    @Volatile
    private var running = false

    init {
        setPriority(priority)
    }

    override fun run() {
        val input = parser.input ?: return parser.close()

        running = true

        try {
            while (running) {
                val message = input.readINDIProtocol() ?: break
                parser.handleMessage(message)
            }
        } catch (_: InterruptedException) {
        } catch (e: Throwable) {
            LOG.error("protocol parser error", e)
            parser.close()
        }
    }

    override fun close() {
        if (!running) return

        running = false

        interrupt()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<INDIProtocolReader>()
    }
}
