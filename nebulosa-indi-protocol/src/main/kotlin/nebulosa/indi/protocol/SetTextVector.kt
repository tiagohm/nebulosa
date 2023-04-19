package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class SetTextVector : SetVector<OneText>(), TextVector<OneText> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setTextVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
