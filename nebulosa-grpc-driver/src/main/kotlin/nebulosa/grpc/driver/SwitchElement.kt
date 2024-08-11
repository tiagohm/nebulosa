package nebulosa.grpc.driver

import nebulosa.grpc.Element

data class SwitchElement(
    override val name: String,
    override val label: String,
    override var value: Boolean = false,
) : PropertyElement<Boolean> {

    fun makeElement() = Element.newBuilder()
        .setName(name)
        .setLabel(label)
        .setEnabled(value)
        .build()
}
