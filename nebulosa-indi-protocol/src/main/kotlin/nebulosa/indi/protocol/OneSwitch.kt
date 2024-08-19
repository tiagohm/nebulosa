package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class OneSwitch(
    override var name: String = "",
    override var value: Boolean = false,
) : OneElement<Boolean>, SwitchElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneSwitch", if (value) "On" else "Off",
        "name", name,
    )
}
