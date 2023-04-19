package nebulosa.indi.protocol

import java.io.PrintStream

class DefNumber : DefElement<Double>(), NumberElement {

    // TODO: Support sexagesimal format conversion.
    override var value = 0.0

    var format = ""

    override var max = 0.0

    override var min = 0.0

    var step = 0.0

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defNumber", value,
        "name", name,
        "label", label,
        "format", format,
        "min", min,
        "max", max,
        "step", step,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefNumber) return false
        if (!super.equals(other)) return false

        if (value != other.value) return false
        if (format != other.format) return false
        if (max != other.max) return false
        if (min != other.min) return false
        return step == other.step
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + min.hashCode()
        result = 31 * result + step.hashCode()
        return result
    }
}
