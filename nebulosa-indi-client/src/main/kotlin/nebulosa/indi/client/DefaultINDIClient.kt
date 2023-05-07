package nebulosa.indi.client

import nebulosa.indi.client.connection.INDIProccessConnection
import nebulosa.indi.client.connection.INDISocketConnection
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.protocol.GetProperties
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.indi.protocol.parser.INDIProtocolReader
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.io.Closeable

class DefaultINDIClient(override val connection: INDIConnection) : INDIClient {

    @Volatile private var closed = false

    private val reader by lazy { INDIProtocolReader(this) }
    private val handlers = arrayListOf<DeviceProtocolHandler>()

    constructor(
        host: String,
        port: Int = INDIProtocol.DEFAULT_PORT,
    ) : this(INDISocketConnection(host, port))

    constructor(
        process: Process,
    ) : this(INDIProccessConnection(process))

    override val input
        get() = connection.input

    override fun start() {
        check(!closed) { "closed" }
        reader.start()
        sendMessageToServer(GetProperties())
    }

    override fun registerDeviceProtocolHandler(handler: DeviceProtocolHandler) {
        handlers.add(handler)
    }

    override fun unregisterDeviceProtocolHandler(handler: DeviceProtocolHandler) {
        handlers.remove(handler)
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        LOG.debug { "sending message: $message" }
        connection.writeINDIProtocol(message)
    }

    override fun handleMessage(message: INDIProtocol) {
        handlers.forEach { it.handleMessage(this, message) }
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

        handlers.forEach(Closeable::close)
        handlers.clear()

        if (thrown != null) {
            throw thrown
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DefaultINDIClient>()
    }
}
