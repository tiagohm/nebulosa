package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class NewTextVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneText> = ArrayList(0),
    override var message: String = "",
    override var timestamp: String = "",
) : NewVector<OneText>, TextVector<OneText> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newTextVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
