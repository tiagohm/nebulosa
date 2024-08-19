package nebulosa.indi.protocol

import nebulosa.indi.protocol.INDIProtocol.Companion.writeXML
import java.io.PrintStream

data class EnableBLOB(
    override var name: String = "",
    override var device: String = "",
    override var message: String = "",
    override var timestamp: String = "",
    var value: BLOBEnable = BLOBEnable.ALSO,
) : INDIProtocol {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "enableBLOB", value,
        "device", device,
        "name", name,
    )
}
