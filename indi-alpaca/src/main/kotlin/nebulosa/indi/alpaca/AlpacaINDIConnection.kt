package nebulosa.indi.alpaca

import nebulosa.alpaca.client.AlpacaClient
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.connection.INDIConnection
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.io.INDIOutputStream
import nebulosa.indi.protocol.parser.INDIProtocolHandler
import java.util.concurrent.LinkedBlockingQueue

class AlpacaINDIConnection(
    val host: String,
    val port: Int,
) : Thread(), INDIConnection, INDIProtocolHandler {

    @JvmField internal val client = AlpacaClient("$host:$port/")
    private val messageQueue = LinkedBlockingQueue<INDIProtocol>()

    @Volatile private var running = false

    override val input = object : INDIInputStream {

        override fun readINDIProtocol() = if (!running) null else messageQueue.take()

        override fun close() = this@AlpacaINDIConnection.close()
    }

    override val output = object : INDIOutputStream {

        override fun writeINDIProtocol(message: INDIProtocol) = handleMessage(message)

        override fun close() = this@AlpacaINDIConnection.close()

        override fun flush() {}
    }

    override val isOpen get() = running

    override fun handleMessage(message: INDIProtocol) {

    }

    override fun run() {
        running = true

        try {
            // call all properties/endpoints.
        } catch (_: InterruptedException) {
        } catch (e: Throwable) {

        }
    }

    override fun close() {
        if (!running) return

        running = false
        messageQueue.clear()

        interrupt()
    }
}
