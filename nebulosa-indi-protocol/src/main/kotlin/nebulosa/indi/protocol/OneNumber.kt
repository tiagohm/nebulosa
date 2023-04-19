package nebulosa.indi.protocol

import java.io.PrintStream

class OneNumber : OneElement<Double>(), NumberElement {

    override val max = 0.0

    override val min = 0.0

    override var value = 0.0

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneNumber", value,
        "name", name,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneNumber) return false
        if (!super.equals(other)) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
