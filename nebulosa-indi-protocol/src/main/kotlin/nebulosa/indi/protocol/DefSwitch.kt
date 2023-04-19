package nebulosa.indi.protocol

import java.io.PrintStream

class DefSwitch : DefElement<Boolean>(), SwitchElement {

    override var value = false

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defSwitch", value,
        "name", name,
        "label", label,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefSwitch) return false
        if (!super.equals(other)) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
