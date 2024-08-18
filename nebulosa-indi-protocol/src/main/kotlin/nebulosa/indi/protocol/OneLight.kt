package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class OneLight(
    override var name: String = "",
    override var value: PropertyState = PropertyState.IDLE,
) : OneElement<PropertyState>, LightElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneLight", value,
        "name", name,
    )
}
