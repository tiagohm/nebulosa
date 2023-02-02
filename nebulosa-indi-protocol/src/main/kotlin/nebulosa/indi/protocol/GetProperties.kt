package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class GetProperties : INDIProtocol() {

    override fun toXML() = XmlBuilder()
        .name("getProperties")
        .attr("version", "1.7")
        .attr("device", device)
        .attr("name", name)
        .build()
}
