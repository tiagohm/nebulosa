package nebulosa.indi.protocol

class DefLight : DefElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE

    override fun toXML() = """<defLight name="$name" label="$label">$value</defLight>"""
}
