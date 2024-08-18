package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefLightVector(
    override var device: String = "",
    override var name: String = "",
    override var label: String = name,
    override var group: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override var perm: PropertyPermission = PropertyPermission.RW,
    override var timeout: Double = 0.0,
    override val elements: MutableList<DefLight> = ArrayList(0),
    override var message: String = "",
    override var timestamp: String = "",
) : DefVector<DefLight>, LightVector<DefLight> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defLightVector", elements,
        "device", device,
        "name", name,
        "label", label,
        "group", group,
        "state", state,
        "timestamp", timestamp,
        "message", message,
    )
}
