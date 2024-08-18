package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefSwitchVector(
    override var device: String = "",
    override var name: String = "",
    override var label: String = name,
    override var group: String = "",
    override var state: PropertyState = PropertyState.IDLE,
    override var perm: PropertyPermission = PropertyPermission.RW,
    var rule: SwitchRule = SwitchRule.ANY_OF_MANY,
    override var timeout: Double = 0.0,
    override val elements: MutableList<DefSwitch> = ArrayList(0),
    override var message: String = "",
    override var timestamp: String = "",
) : DefVector<DefSwitch>, SwitchVector<DefSwitch> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defSwitchVector", elements,
        "device", device,
        "name", name,
        "label", label,
        "group", group,
        "state", state,
        "perm", perm,
        "rule", rule,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
