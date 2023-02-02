package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class DelProperty : INDIProtocol() {

    override fun toXML() = XmlBuilder()
        .name("delProperty")
        .attr("device", device)
        .attr("name", name)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .build()
}
