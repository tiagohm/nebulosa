package nebulosa.indi.protocol

import java.io.PrintStream

class GetProperties : INDIProtocol() {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "getProperties", null,
        "version", "1.7",
        "device", device,
        "name", name,
    )
}
