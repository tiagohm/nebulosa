package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class Message : INDIProtocol() {

    override fun toXML() = XmlBuilder()
        .name("message")
        .attr("device", device)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .build()
}
