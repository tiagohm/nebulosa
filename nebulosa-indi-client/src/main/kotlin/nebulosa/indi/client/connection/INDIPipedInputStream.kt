package nebulosa.indi.client.connection

import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.indi.protocol.io.INDIInputStream
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
