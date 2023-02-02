package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class NewSwitchVector : NewVector<OneSwitch>(), SwitchVector<OneSwitch> {

    override fun toXML() = XmlBuilder()
        .name("newSwitchVector")
        .attr("device", device)
        .attr("name", name)
        .attr("timestamp", timestamp)
        .value(elements.toXML())
        .build()
}
