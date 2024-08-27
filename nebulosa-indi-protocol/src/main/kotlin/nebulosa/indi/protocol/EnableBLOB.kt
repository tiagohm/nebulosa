package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class EnableBLOB(
    override var device: String = "",
    override var name: String = "",
    var value: BLOBEnable = BLOBEnable.ALSO,
    override var message: String = "",
    override var timestamp: String = "",
) : INDIProtocol {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "enableBLOB", value,
        "device", device,
        "name", name,
    )
}
