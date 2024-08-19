package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class NewNumberVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneNumber> = ArrayList(0),
    override var message: String = "",
    override var timestamp: String = "",
) : NewVector<OneNumber>, NumberVector<OneNumber>, MinMaxVector<OneNumber> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newNumberVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
