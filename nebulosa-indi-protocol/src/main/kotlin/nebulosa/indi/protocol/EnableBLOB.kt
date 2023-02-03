package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class EnableBLOB : INDIProtocol() {

    var value = BLOBEnable.ALSO

    override fun toXML() = XmlBuilder()
        .name("enableBLOB")
        .attr("device", device)
        .attr("name", name)
        .value(value)
        .build()
}
