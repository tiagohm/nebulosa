package nebulosa.indi.protocol

import java.io.PrintStream

class Message : INDIProtocol() {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "message",
        "device", device,
        "timestamp", timestamp,
        "message", message,
    )
}
