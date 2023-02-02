package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class NewNumberVector : NewVector<OneNumber>(), MinMaxVector<OneNumber> {

    override fun toXML() = XmlBuilder()
        .name("newNumberVector")
        .attr("device", device)
        .attr("name", name)
        .attr("timestamp", timestamp)
        .value(elements.toXML())
        .build()
}
