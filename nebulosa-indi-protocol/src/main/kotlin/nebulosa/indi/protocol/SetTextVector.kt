package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class SetTextVector : SetVector<OneText>(), TextVector<OneText> {

    override fun toXML() = XmlBuilder()
        .name("setTextVector")
        .attr("device", device)
        .attr("name", name)
        .attr("state", state)
        .attr("timeout", timeout)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
