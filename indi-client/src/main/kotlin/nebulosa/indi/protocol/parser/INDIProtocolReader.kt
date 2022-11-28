package nebulosa.indi.protocol.parser

import java.io.Closeable

class INDIProtocolReader(
    val parser: INDIProtocolParser,
    priority: Int = NORM_PRIORITY,
) : Thread(), Closeable {

    @Volatile
    private var running = false

    init {
        setPriority(priority)
    }

    override fun run() {
        val input = parser.input ?: return parser.close()

        running = true

        try {
            while (running) {
                val message = input.readINDIProtocol() ?: break
                parser.handleMessage(message)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: Throwable) {
            e.printStackTrace()
            parser.close()
        }
    }

    override fun close() {
        if (!running) return

        running = false

        interrupt()
    }
}
