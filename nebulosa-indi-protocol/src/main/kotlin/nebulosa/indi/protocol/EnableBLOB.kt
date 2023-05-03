package nebulosa.indi.protocol

import java.io.PrintStream

class EnableBLOB : INDIProtocol() {

    var value = BLOBEnable.ALSO

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "enableBLOB", value,
        "device", device,
        "name", name,
    )
}
