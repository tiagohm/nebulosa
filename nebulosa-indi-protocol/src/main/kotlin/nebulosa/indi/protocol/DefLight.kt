package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class DefLight : DefElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE

    override fun toXML() = XmlBuilder()
        .name("defLight")
        .attr("name", name)
        .attr("label", label)
        .value(value)
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefLight) return false
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
