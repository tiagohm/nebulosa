package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class GetProperties(
    override var device: String = "",
    override var name: String = "",
    override var message: String = "",
    override var timestamp: String = "",
) : INDIProtocol {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "getProperties", null,
        "version", "1.7",
        "device", device,
        "name", name,
    )
}
