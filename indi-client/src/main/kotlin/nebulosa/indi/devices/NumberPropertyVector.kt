package nebulosa.indi.devices

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState

data class NumberPropertyVector(
    // override val device: Device,
    override val name: String,
    override val label: String,
    override val group: String,
    override val perm: PropertyPermission,
    override var state: PropertyState,
    internal val properties: LinkedHashMap<String, NumberProperty>,
) : PropertyVector<Double, NumberProperty>, Map<String, NumberProperty> by properties {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumberPropertyVector) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()
}
