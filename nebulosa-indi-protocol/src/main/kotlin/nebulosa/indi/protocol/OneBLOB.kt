package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class OneBLOB(
    override var name: String = "",
    override var value: String = "",
    var format: String = "",
    var size: String = "",
) : OneElement<String>, BLOBElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneBLOB", value,
        "name", name,
        "format", format,
        "size", size,
    )
}

