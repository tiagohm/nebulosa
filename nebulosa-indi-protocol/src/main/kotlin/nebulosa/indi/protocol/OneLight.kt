package nebulosa.indi.protocol

import java.io.PrintStream

class OneLight : OneElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneLight", value,
        "name", name,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneLight) return false
        if (!super.equals(other)) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
