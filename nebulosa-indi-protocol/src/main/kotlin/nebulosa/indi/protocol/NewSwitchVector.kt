package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class NewSwitchVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneSwitch> = ArrayList(0),
    override var message: String = "",
    override var timestamp: String = "",
) : NewVector<OneSwitch>, SwitchVector<OneSwitch> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newSwitchVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
