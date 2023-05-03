package nebulosa.indi.protocol

import java.io.PrintStream

class OneSwitch : OneElement<Boolean>(), SwitchElement {

    override var value = false

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneSwitch", if (value) "On" else "Off",
        "name", name,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneSwitch) return false
        if (!super.equals(other)) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
