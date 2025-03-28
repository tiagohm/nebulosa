package nebulosa.indi.protocol.parser

import nebulosa.log.d
import nebulosa.log.loggerFor

class INDIProtocolReader(
    private val parser: INDIProtocolParser,
    priority: Int = NORM_PRIORITY,
) : Thread("INDI Protocol Reader"), AutoCloseable {

    private val listeners = LinkedHashSet<CloseConnectionListener>(1)

    @Volatile private var running = false

    init {
        isDaemon = true
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

            LOG.d { info("protocol parser finished") }
            listeners.onEach { it.onConnectionClosed() }.clear()
            parser.close()
        } catch (_: InterruptedException) {
            currentThread().interrupt()
            running = false
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
        listeners.clear()

        interrupt()
    }

    companion object {

        private val LOG = loggerFor<INDIProtocolReader>()
    }
}
