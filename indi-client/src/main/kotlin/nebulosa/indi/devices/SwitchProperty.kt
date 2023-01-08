package nebulosa.indi.devices

data class SwitchProperty(
    override val name: String,
    override val label: String,
    override var value: Boolean,
) : Property<Boolean>
