package nebulosa.indi.devices

data class SwitchProperty(
    override val name: String,
    override val label: String,
    override val value: Boolean,
) : Property<Boolean>
