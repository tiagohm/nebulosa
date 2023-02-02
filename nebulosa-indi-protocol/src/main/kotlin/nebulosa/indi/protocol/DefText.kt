package nebulosa.indi.protocol

class DefText : DefElement<String>(), TextElement {

    override var value = ""

    override fun toXML() = """<defText name="$name" label="$label">$value</defText>"""
}
