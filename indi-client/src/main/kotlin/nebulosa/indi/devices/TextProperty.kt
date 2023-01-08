package nebulosa.indi.devices

data class TextProperty(
    override val name: String,
    override val label: String,
    override var value: String,
) : Property<String>
