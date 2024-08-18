package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class OneNumber(
    override var name: String = "",
    override var value: Double = 0.0,
) : OneElement<Double>, NumberElement {

    override val max = 0.0
    override val min = 0.0

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "oneNumber", value,
        "name", name,
    )
}
