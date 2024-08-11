package nebulosa.grpc.driver

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState

sealed interface Property<T> {

    val name: String

    val label: String

    val group: String

    val permission: PropertyPermission

    var state: PropertyState

    val elements: List<PropertyElement<T>>

    operator fun contains(name: String): Boolean
}
