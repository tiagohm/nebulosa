package nebulosa.indi.device

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState

sealed interface PropertyVector<T, P : Property<T>> : Map<String, P> {

    val device: Device

    val name: String

    val label: String

    val group: String

    val perm: PropertyPermission

    val state: PropertyState
}
