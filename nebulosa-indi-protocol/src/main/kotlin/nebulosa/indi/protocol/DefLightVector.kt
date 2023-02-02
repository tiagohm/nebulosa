package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class DefLightVector : DefVector<DefLight>(), LightVector<DefLight> {

    override fun toXML() = XmlBuilder()
        .name("defLightVector")
        .attr("device", device)
        .attr("name", name)
        .attr("label", label)
        .attr("group", group)
        .attr("state", state)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
