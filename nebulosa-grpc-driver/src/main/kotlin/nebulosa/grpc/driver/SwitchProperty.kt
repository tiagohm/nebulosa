package nebulosa.grpc.driver

import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SwitchRule

internal data class SwitchProperty(
    override val name: String,
    override val label: String,
    override val group: String,
    override val permission: PropertyPermission = PropertyPermission.RW,
    val rule: SwitchRule = SwitchRule.ONE_OF_MANY,
    override var state: PropertyState = PropertyState.IDLE,
    override val elements: MutableList<SwitchElement> = ArrayList(2),
) : Property<Boolean> {

    override fun contains(name: String) = elements.any { it.name == name }

    fun isOn(name: String) = elements.firstOrNull { it.name == name }?.value == true

    fun isOff(name: String) = elements.firstOrNull { it.name == name }?.value == false

    fun on(name: String): Boolean {
        // TODO: Ver se a regra está certa!
        return if (rule != SwitchRule.ANY_OF_MANY) {
            elements.forEach { it.value = it.name == name }
            true
        } else {
            val element = elements.firstOrNull { it.name == name } ?: return false
            element.value = true
            true
        }
    }
}
