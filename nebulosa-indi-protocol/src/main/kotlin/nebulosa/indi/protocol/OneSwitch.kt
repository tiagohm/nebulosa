package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class OneSwitch : OneElement<Boolean>(), SwitchElement {

    override var value = false

    override fun toXML() = XmlBuilder()
        .name("oneSwitch")
        .attr("name", name)
        .value(if (value) "On" else "Off")
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneSwitch) return false
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
