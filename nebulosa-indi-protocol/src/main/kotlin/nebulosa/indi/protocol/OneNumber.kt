package nebulosa.indi.protocol

class OneNumber : OneElement<Double>(), NumberElement {

    override val max = 0.0

    override val min = 0.0

    override var value = 0.0

    override fun toXML() = """<oneNumber name="$name">$value</oneNumber>"""
}
