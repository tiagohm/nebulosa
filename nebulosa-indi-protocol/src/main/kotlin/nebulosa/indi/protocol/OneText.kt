package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class OneText(
    override var name: String = "",
    override var value: String = "",
) : OneElement<String>, TextElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneText", value,
        "name", name,
    )
}
