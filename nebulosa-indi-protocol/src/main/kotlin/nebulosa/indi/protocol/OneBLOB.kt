package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class OneBLOB : OneElement<String>(), BLOBElement {

    var format = ""

    var size = ""

    override var value = ""

    override fun toXML() = XmlBuilder()
        .name("oneBLOB")
        .attr("name", name)
        .attr("format", format)
        .attr("size", size)
        .value(value)
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneBLOB) return false
        if (!super.equals(other)) return false

        if (format != other.format) return false
        if (size != other.size) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

