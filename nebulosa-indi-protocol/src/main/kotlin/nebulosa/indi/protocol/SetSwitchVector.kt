package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class SetSwitchVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneSwitch> = ArrayList(0),
    override var timeout: Double = 0.0,
    override var message: String = "",
    override var timestamp: String = "",
) : SetVector<OneSwitch>, SwitchVector<OneSwitch> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setSwitchVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
