package nebulosa.indi.protocol.connection

import nebulosa.indi.protocol.INDIProtocol
import java.util.concurrent.BlockingQueue

class INDIPipedConnection(
    input: BlockingQueue<INDIProtocol>,
    output: BlockingQueue<INDIProtocol>,
) : INDIConnection {

    @Volatile
    private var closed = false

    override val input = INDIPipedInputStream(this, input)

    override val output = INDIPipedOutputStream(this, output)

    override val isOpen get() = !closed

    override fun close() {
        if (closed) return

        closed = true
    }
}
