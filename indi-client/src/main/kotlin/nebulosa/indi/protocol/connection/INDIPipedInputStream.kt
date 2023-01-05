package nebulosa.indi.protocol.connection

import nebulosa.indi.protocol.EndMarker
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIInputStream
import java.io.Closeable
import java.util.concurrent.BlockingQueue

class INDIPipedInputStream(
    private val connection: INDIConnection,
    private val queue: BlockingQueue<INDIProtocol>,
) : INDIInputStream, Closeable by connection {

    override fun readINDIProtocol(): INDIProtocol? {
        return if (connection.isOpen) {
            null
        } else try {
            queue.take().takeIf { it !is EndMarker }
        } catch (e: InterruptedException) {
            close()
            null
        }
    }
}
