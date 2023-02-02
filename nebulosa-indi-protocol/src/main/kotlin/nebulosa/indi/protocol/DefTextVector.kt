package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class DefTextVector : DefVector<DefText>(), TextVector<DefText> {

    override fun toXML() = XmlBuilder()
        .name("defTextVector")
        .attr("device", device)
        .attr("name", name)
        .attr("label", label)
        .attr("group", group)
        .attr("state", state)
        .attr("perm", perm)
        .attr("timeout", timeout)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
