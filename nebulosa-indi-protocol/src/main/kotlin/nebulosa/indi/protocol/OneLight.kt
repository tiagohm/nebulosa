package nebulosa.indi.protocol

class OneLight : OneElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE

    override fun toXML() = """<oneLight name="$name">$value</oneLight>"""
}
