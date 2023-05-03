package nebulosa.indi.protocol

import java.io.PrintStream

class DelProperty : INDIProtocol() {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "delProperty", null,
        "device", device,
        "name", name,
        "timestamp", timestamp,
        "message", message,
    )
}
