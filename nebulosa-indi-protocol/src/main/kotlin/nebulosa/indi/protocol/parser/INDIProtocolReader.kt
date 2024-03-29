package nebulosa.indi.protocol.parser

import nebulosa.log.loggerFor
import java.io.Closeable

class INDIProtocolReader(
    private val parser: INDIProtocolParser,
    priority: Int = NORM_PRIORITY,
) : Thread(), Closeable {

    private val listeners = HashSet<CloseConnectionListener>(1)

    @Volatile private var running = false

    init {
        setPriority(priority)
    }

    val isRunning
        get() = running

    fun registerCloseConnectionListener(listener: CloseConnectionListener) {
        listeners.add(listener)
    }

    fun unregisterCloseConnectionListener(listener: CloseConnectionListener) {
        listeners.remove(listener)
    }

    override fun start() {
        running = true
        super.start()
    }

    override fun run() {
        val input = parser.input

        try {
            while (running) {
                val message = input?.readINDIProtocol() ?: break
                parser.handleMessage(message)
            }

            LOG.info("protocol parser finished")
            listeners.onEach { it.onConnectionClosed() }.clear()
            parser.close()
        } catch (_: InterruptedException) {
            running = false
            LOG.error("protocol parser interrupted")
        } catch (e: Throwable) {
            running = false
            listeners.onEach { it.onConnectionClosed() }.clear()
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
