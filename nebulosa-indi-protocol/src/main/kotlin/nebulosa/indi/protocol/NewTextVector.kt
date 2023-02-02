package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class NewTextVector : NewVector<OneText>(), TextVector<OneText> {

    override fun toXML() = XmlBuilder()
        .name("newTextVector")
        .attr("device", device)
        .attr("name", name)
        .attr("timestamp", timestamp)
        .value(elements.toXML())
        .build()
}
