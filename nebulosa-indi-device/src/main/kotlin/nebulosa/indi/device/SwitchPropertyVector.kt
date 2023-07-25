package nebulosa.indi.device

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SwitchRule

data class SwitchPropertyVector(
    override val device: Device,
    override val name: String,
    override val label: String,
    override val group: String,
    override val perm: PropertyPermission,
    val rule: SwitchRule,
    override var state: PropertyState,
    internal val properties: LinkedHashMap<String, SwitchProperty>,
) : PropertyVector<Boolean, SwitchProperty>, Map<String, SwitchProperty> by properties {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SwitchPropertyVector) return false
        return name == other.name
    }

    override fun hashCode() = name.hashCode()
}
