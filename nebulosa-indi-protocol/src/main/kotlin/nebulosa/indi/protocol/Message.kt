package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class Message(
    override var device: String = "",
    override var name: String = "",
    override var message: String = "",
    override var timestamp: String = "",
) : INDIProtocol {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "message",
        "device", device,
        "timestamp", timestamp,
        "message", message,
    )
}
