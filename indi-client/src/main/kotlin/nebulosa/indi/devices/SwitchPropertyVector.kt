package nebulosa.indi.devices

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SwitchRule

data class SwitchPropertyVector(
    // override val device: Device,
    override val name: String,
    override val label: String,
    override val group: String,
    override val perm: PropertyPermission,
    var rule: SwitchRule,
    override val state: PropertyState,
    val properties: LinkedHashMap<String, SwitchProperty>,
) : PropertyVector<Boolean, SwitchProperty>, Map<String, SwitchProperty> by properties
