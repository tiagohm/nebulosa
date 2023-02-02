package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class OneNumber : OneElement<Double>(), NumberElement {

    override val max = 0.0

    override val min = 0.0

    override var value = 0.0

    override fun toXML() = XmlBuilder()
        .name("oneNumber")
        .attr("name", name)
        .value(value)
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneNumber) return false
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
