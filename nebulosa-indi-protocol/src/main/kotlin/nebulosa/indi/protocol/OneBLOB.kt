package nebulosa.indi.protocol

import java.io.PrintStream

class OneBLOB : OneElement<String>(), BLOBElement {

    var format = ""

    var size = ""

    override var value = ""

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneBLOB", value,
        "name", name,
        "format", format,
        "size", size,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneBLOB) return false
        if (!super.equals(other)) return false

        if (format != other.format) return false
        if (size != other.size) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

