package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

class DefSwitchVector : DefVector<DefSwitch>(), SwitchVector<DefSwitch> {

    @JvmField var rule = SwitchRule.ANY_OF_MANY

    override fun toXML() =
        """<defSwitchVector device="$device" name="$name" label="$label" group="$group" state="$state" perm="$perm" rule="$rule" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</defSwitchVector>"""

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
