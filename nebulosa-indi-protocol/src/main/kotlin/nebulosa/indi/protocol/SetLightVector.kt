package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class SetLightVector : SetVector<OneLight>(), LightVector<OneLight> {

    override fun toXML() = XmlBuilder()
        .name("setLightVector")
        .attr("device", device)
        .attr("name", name)
        .attr("state", state)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
