package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class OneLight : OneElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE

    override fun toXML() = XmlBuilder()
        .name("oneLight")
        .attr("name", name)
        .value(value)
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneLight) return false
        if (!super.equals(other)) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
