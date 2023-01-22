package nebulosa.indi.device

data class TextProperty(
    override val name: String,
    override val label: String,
    override var value: String,
) : Property<String>
