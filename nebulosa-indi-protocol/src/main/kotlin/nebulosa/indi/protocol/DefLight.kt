package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefLight(
    override var name: String = "",
    override var label: String = name,
    override var value: PropertyState = PropertyState.IDLE,
) : DefElement<PropertyState>, LightElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defLight", value,
        "name", name,
        "label", label,
    )
}
