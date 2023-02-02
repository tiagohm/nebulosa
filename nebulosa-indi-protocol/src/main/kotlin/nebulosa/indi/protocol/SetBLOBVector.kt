package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class SetBLOBVector : SetVector<OneBLOB>(), BLOBVector<OneBLOB> {

    override fun toXML() = XmlBuilder()
        .name("setBLOBVector")
        .attr("device", device)
        .attr("name", name)
        .attr("state", state)
        .attr("timeout", timeout)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()
}
