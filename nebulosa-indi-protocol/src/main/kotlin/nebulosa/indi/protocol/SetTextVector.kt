package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class SetTextVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneText> = ArrayList(0),
    override var timeout: Double = 0.0,
    override var message: String = "",
    override var timestamp: String = "",
) : SetVector<OneText>, TextVector<OneText> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setTextVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
