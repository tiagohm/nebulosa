package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class DefNumber(
    override var name: String = "",
    override var label: String = name,
    override var value: Double = 0.0,
    var format: String = "", // TODO: Support sexagesimal format conversion.
    override var max: Double = 0.0,
    override var min: Double = 0.0,
    var step: Double = 0.0,
) : DefElement<Double>, NumberElement {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defNumber", value,
        "name", name,
        "label", label,
        "format", format,
        "min", min,
        "max", max,
        "step", step,
    )
}
