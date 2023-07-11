package nebulosa.indi.device

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState

data class TextPropertyVector(
    // override val device: Device,
    override val name: String,
    override val label: String,
    override val group: String,
    override val perm: PropertyPermission,
    override var state: PropertyState,
    internal val properties: LinkedHashMap<String, TextProperty>,
) : PropertyVector<String, TextProperty>, Map<String, TextProperty> by properties {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextPropertyVector) return false
        return name == other.name
    }

    override fun hashCode() = name.hashCode()
}
