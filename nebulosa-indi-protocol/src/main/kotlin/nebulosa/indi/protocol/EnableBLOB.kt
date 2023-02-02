package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class EnableBLOB : INDIProtocol() {

    @JvmField var value = BLOBEnable.ALSO

    override fun toXML() = XmlBuilder()
        .name("enableBLOB")
        .attr("device", device)
        .attr("name", name)
        .value(value)
        .build()
}
