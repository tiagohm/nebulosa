package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class SetNumberVector : SetVector<OneNumber>(), NumberVector<OneNumber> {

    override fun toXML() = XmlBuilder()
        .name("setNumberVector")
        .attr("device", device)
        .attr("name", name)
        .attr("state", state)
        .attr("timeout", timeout)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
