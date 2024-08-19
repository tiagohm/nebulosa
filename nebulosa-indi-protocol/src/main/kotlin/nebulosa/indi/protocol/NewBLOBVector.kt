package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class NewBLOBVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneBLOB> = ArrayList(0),
    override var message: String = "",
    override var timestamp: String = "",
) : NewVector<OneBLOB>, BLOBVector<OneBLOB> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newBLOBVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
