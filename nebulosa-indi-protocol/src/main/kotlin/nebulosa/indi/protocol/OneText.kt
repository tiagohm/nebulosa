package nebulosa.indi.protocol

class OneText : OneElement<String>(), TextElement {

    override var value = ""

    override fun toXML() = """<oneText name="$name">$value</oneText>"""
}
