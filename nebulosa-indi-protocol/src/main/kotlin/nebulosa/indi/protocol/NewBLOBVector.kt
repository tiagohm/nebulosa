package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

@Suppress("CanSealedSubClassBeObject")
class NewBLOBVector : NewVector<OneBLOB>(), BLOBVector<OneBLOB> {

    override fun toXML() = XmlBuilder()
        .name("newBLOBVector")
        .attr("device", device)
        .attr("name", name)
        .attr("timestamp", timestamp)
        .value(elements.toXML())
        .build()
}
