package nebulosa.alpaca.client

import nebulosa.alpaca.api.AlpacaService
import nebulosa.indi.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue

class AlpacaClient(url: String) : INDIProtocolParser, MessageSender, Closeable {

    @Volatile private var closed = false

    private val service by lazy { AlpacaService(url) }
    private val reader by lazy { INDIProtocolReader(this) }
    private val handlers = arrayListOf<DeviceProtocolHandler>()
    private val messageQueue = LinkedBlockingQueue<INDIProtocol>()

    override val input = object : INDIInputStream {

        override fun readINDIProtocol() = if (closed) null else messageQueue.take()

        override fun close() = this@AlpacaClient.close()
    }

    fun start() {
        check(!closed) { "closed" }
        reader.start()
    }

    override fun handleMessage(message: INDIProtocol) {
        handlers.forEach { it.handleMessage(this, message) }
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        TODO("Not yet implemented")
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

        handlers.forEach(Closeable::close)
        handlers.clear()

        if (thrown != null) {
            throw thrown
        }
    }
}
