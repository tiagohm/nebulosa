package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class SetNumberVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneNumber> = ArrayList(0),
    override var timeout: Double = 0.0,
    override var message: String = "",
    override var timestamp: String = "",
) : SetVector<OneNumber>, NumberVector<OneNumber> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setNumberVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
