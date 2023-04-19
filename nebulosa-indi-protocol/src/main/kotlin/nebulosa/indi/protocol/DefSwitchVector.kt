package nebulosa.indi.protocol

import java.io.PrintStream

class DefSwitchVector : DefVector<DefSwitch>(), SwitchVector<DefSwitch> {

    var rule = SwitchRule.ANY_OF_MANY

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defSwitchVector", elements,
        "device", device,
        "name", name,
        "label", label,
        "group", group,
        "state", state,
        "perm", perm,
        "rule", rule,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefSwitchVector) return false
        if (!super.equals(other)) return false

        return rule == other.rule
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + rule.hashCode()
        return result
    }
}
