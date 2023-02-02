package nebulosa.indi.protocol

class OneSwitch : OneElement<Boolean>(), SwitchElement {

    override var value = false

    override fun toXML() = """<oneSwitch name="$name">$value</oneSwitch>"""
}
