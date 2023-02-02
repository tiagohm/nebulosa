package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class NewLightVector : NewVector<OneLight>(), LightVector<OneLight> {

    override fun toXML() = XmlBuilder()
        .name("newLightVector")
        .attr("device", device)
        .attr("name", name)
        .attr("timestamp", timestamp)
        .value(elements.toXML())
        .build()
}
