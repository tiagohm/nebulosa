package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefSwitch(
    override var name: String = "",
    override var label: String = name,
    override var value: Boolean = false,
) : DefElement<Boolean>, SwitchElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defSwitch", value,
        "name", name,
        "label", label,
    )
}
