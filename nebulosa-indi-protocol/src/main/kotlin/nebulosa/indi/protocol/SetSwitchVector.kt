package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class SetSwitchVector : SetVector<OneSwitch>(), SwitchVector<OneSwitch> {

    override fun toXML() = XmlBuilder()
        .name("setSwitchVector")
        .attr("device", device)
        .attr("name", name)
        .attr("state", state)
        .attr("timeout", timeout)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
