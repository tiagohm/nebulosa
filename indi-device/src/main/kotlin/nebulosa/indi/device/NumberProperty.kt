package nebulosa.indi.device

data class NumberProperty(
    override val name: String,
    override val label: String,
    override var value: Double,
) : Property<Double>
