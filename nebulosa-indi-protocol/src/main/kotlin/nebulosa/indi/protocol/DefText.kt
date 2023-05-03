package nebulosa.indi.protocol

import java.io.PrintStream

class DefText : DefElement<String>(), TextElement {

    override var value = ""

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defText", value,
        "name", name,
        "label", label,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefText) return false
        if (!super.equals(other)) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
