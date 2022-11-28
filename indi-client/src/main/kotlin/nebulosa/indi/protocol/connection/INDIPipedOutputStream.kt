package nebulosa.indi.protocol.connection

import nebulosa.indi.protocol.EndMarker
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIOutputStream
import java.util.concurrent.BlockingQueue

class INDIPipedOutputStream(
    private val connection: INDIConnection,
    private val queue: BlockingQueue<INDIProtocol>,
) : INDIOutputStream {

    override fun writeINDIProtocol(message: INDIProtocol) {
        try {
            queue.put(message)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            close()
        }
    }

    override fun flush() = Unit

    override fun close() {
        try {
            connection.close()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            queue.put(EndMarker)
        }
    }
}
