package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefText(
    override var name: String = "",
    override var label: String = name,
    override var value: String = "",
) : DefElement<String>, TextElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defText", value,
        "name", name,
        "label", label,
    )
}
