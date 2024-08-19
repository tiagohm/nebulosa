package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DelProperty(
    override var device: String = "",
    override var name: String = "",
    override var message: String = "",
    override var timestamp: String = "",
) : INDIProtocol {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "delProperty", null,
        "device", device,
        "name", name,
        "timestamp", timestamp,
        "message", message,
    )
}
