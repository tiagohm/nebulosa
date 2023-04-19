package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class DefBLOB : DefElement<String>(), BLOBElement {

    override var value = ""

    override fun toXML() = XmlBuilder()
        .name("defBLOB")
        .attr("name", name)
        .attr("label", label)
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefBLOB) return false
        if (!super.equals(other)) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
