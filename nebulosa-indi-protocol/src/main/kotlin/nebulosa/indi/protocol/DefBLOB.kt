package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefBLOB(
    override var name: String = "",
    override var label: String = name,
) : DefElement<String>, BLOBElement {

    override var value = ""

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defBLOB", null,
        "name", name,
        "label", label,
    )
}
