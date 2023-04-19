package nebulosa.indi.client.connection

import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.indi.protocol.io.INDIOutputStream
import java.util.concurrent.BlockingQueue

class INDIPipedOutputStream(
    private val connection: INDIConnection,
    private val queue: BlockingQueue<INDIProtocol>,
) : INDIOutputStream {

    override fun writeINDIProtocol(message: INDIProtocol) {
        try {
            queue.put(message)
        } catch (e: Throwable) {
            close()
        }
    }

    override fun flush() = Unit

    override fun close() = connection.close()
}
