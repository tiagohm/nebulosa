package nebulosa.indi.client.connection

import nebulosa.indi.connection.INDIConnection
import nebulosa.indi.connection.io.INDIInputStream
import nebulosa.indi.protocol.INDIProtocol
import java.io.Closeable
import java.util.concurrent.BlockingQueue

class INDIPipedInputStream(
    private val connection: INDIConnection,
    private val queue: BlockingQueue<INDIProtocol>,
) : INDIInputStream, Closeable by connection {

    override fun readINDIProtocol(): INDIProtocol? {
        return if (!connection.isOpen) {
            null
        } else try {
            queue.take()
        } catch (e: Throwable) {
            close()
            null
        }
    }
}
