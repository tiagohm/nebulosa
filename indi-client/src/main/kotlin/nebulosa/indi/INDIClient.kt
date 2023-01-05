package nebulosa.indi

import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.protocol.GetProperties
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.Vector
import nebulosa.indi.protocol.connection.INDIConnection
import nebulosa.indi.protocol.connection.INDIProccessConnection
import nebulosa.indi.protocol.connection.INDISocketConnection
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import org.slf4j.LoggerFactory
import java.io.Closeable

class INDIClient(val connection: INDIConnection) : INDIProtocolParser, Closeable {

    private var started = false
    private var closed = false
    private val reader by lazy { INDIProtocolReader(this) }
    private var handlers = ArrayList<DeviceProtocolHandler>()

    constructor(
        host: String,
        port: Int = INDIProtocol.DEFAULT_PORT,
    ) : this(INDISocketConnection(host, port))

    constructor(
        process: Process,
    ) : this(INDIProccessConnection(process))

    override val input get() = connection.input

    fun start() {
        if (started) return
        reader.start()
        sendMessageToServer(GetProperties())
        started = true
    }

    fun registerDeviceProtocolHandler(handler: DeviceProtocolHandler) {
        handlers.add(handler)
    }

    fun unregisterDeviceProtocolHandler(handler: DeviceProtocolHandler) {
        handlers.remove(handler)
    }

    fun sendMessageToServer(message: INDIProtocol) {
        if (LOG.isDebugEnabled) {
            LOG.debug(
                "SENDING: {}: {}: {}: {}",
                message::class.simpleName,
                message.device,
                message.name,
                (message as? Vector<*>)?.joinToString(", ") { "${it.name}=${it.value}" })
        }

        connection.writeINDIProtocol(message)
    }

    override fun handleMessage(message: INDIProtocol) {
        synchronized(this) { handlers.forEach { it.handleMessage(this, message) } }
    }

    override fun close() {
        if (closed) return

        closed = true

        var thrown: Throwable? = null

        try {
            reader.close()
        } catch (e: Throwable) {
            thrown = e
        }

        try {
            connection.close()
        } catch (e: Throwable) {
            if (thrown == null) {
                thrown = e
            }
        }

        handlers.forEach { it.close() }
        handlers.clear()

        if (thrown != null) {
            throw thrown
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(INDIClient::class.java)
    }
}
