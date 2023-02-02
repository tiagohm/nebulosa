package nebulosa.indi.protocol

import nebulosa.indi.protocol.xml.XmlBuilder

class DefSwitchVector : DefVector<DefSwitch>(), SwitchVector<DefSwitch> {

    @JvmField var rule = SwitchRule.ANY_OF_MANY

    override fun toXML() = XmlBuilder()
        .name("defSwitchVector")
        .attr("device", device)
        .attr("name", name)
        .attr("label", label)
        .attr("group", group)
        .attr("state", state)
        .attr("perm", perm)
        .attr("rule", rule)
        .attr("timeout", timeout)
        .attr("timestamp", timestamp)
        .attr("message", message)
        .value(elements.toXML())
        .build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefSwitchVector) return false
        if (!super.equals(other)) return false

        if (rule != other.rule) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + rule.hashCode()
        return result
    }
}
