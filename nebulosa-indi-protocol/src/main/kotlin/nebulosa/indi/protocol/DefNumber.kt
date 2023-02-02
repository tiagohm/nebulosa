package nebulosa.indi.protocol

class DefNumber : DefElement<Double>(), NumberElement {

    // TODO: Support sexagesimal format conversion.
    override var value = 0.0

    @JvmField var format = ""

    override var max = 0.0

    override var min = 0.0

    @JvmField var step = 0.0

    override fun toXML() = """<defNumber name="$name" label="$label" format="$format" min="$min" max="$max" step="$step">$value</defNumber>"""
}
