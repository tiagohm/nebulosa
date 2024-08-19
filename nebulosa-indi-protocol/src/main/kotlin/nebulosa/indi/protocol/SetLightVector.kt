package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class SetLightVector(
    override var device: String = "",
    override var name: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<OneLight> = ArrayList(0),
    override var timeout: Double = 0.0,
    override var message: String = "",
    override var timestamp: String = "",
) : SetVector<OneLight>, LightVector<OneLight> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setLightVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timestamp", timestamp,
        "message", message,
    )
}
