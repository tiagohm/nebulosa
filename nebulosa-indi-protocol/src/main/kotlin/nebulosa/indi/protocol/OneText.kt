package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class OneText : OneElement<String>(), TextElement {

    override var value = ""

    override fun toXML() = XmlBuilder()
        .name("oneText")
        .attr("name", name)
        .value(value)
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneText) return false
        if (!super.equals(other)) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
